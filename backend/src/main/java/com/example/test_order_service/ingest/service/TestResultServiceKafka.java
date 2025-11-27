package com.example.test_order_service.ingest.service;

import com.example.test_order_service.dto.response.RestResponse;
import com.example.test_order_service.dto.response.TestResultResponse;
import com.example.test_order_service.entity.TestOrder;
import com.example.test_order_service.entity.TestResult;
import com.example.test_order_service.entity.TestResultParameter;
import com.example.test_order_service.entity.enumForEntity.TestOrderStatus;
import com.example.test_order_service.exception.ResourceNotFoundException;
import com.example.test_order_service.ingest.publisher.TestResultEventPublisher;
import com.example.test_order_service.mapper.TestResultMapper;
import com.example.test_order_service.repository.TestOrderRepository;
import com.example.test_order_service.repository.TestResultRepository;
import com.example.test_order_service.service.TestResultService;

import com.example.test_order_service.utils.GeneralUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;


@Service
@Primary
@Slf4j
public class TestResultServiceKafka implements TestResultService {
    private final TestResultRepository testResultRepository;
    private final TestOrderRepository testOrderRepository;
    private final TestResultMapper testResultMapper;
    private final TestResultEventPublisher eventPublisher;

    private TestResultServiceKafka self;

    public TestResultServiceKafka(TestResultRepository testResultRepository,
                                  TestOrderRepository testOrderRepository,
                                  TestResultMapper testResultMapper,
                                  TestResultEventPublisher eventPublisher) {
        this.testResultRepository = testResultRepository;
        this.testOrderRepository = testOrderRepository;
        this.testResultMapper = testResultMapper;
        this.eventPublisher = eventPublisher;
    }

    @Autowired
    public void setSelf(@Lazy TestResultServiceKafka self) {
        this.self = self;
    }

    @Transactional(readOnly = true)
    public RestResponse<?> getResultByBloodCollectionId(String bloodCollectionId) {
        TestResult testResult = testResultRepository.findByBloodCollectionId(bloodCollectionId)
                .orElseThrow(() -> new IllegalArgumentException("Test result not found for blood collection Id: " + bloodCollectionId));

        TestResultResponse testResultResponse = testResultMapper.toTestResultResponse(testResult);

        return RestResponse.<TestResultResponse>builder()
                .statusCode(200)
                .result(testResultResponse)
                .message("Retrieved test result successfully")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RestResponse<TestResultResponse> receiveHl7(String rawHl7) {
        // Setp 1: Basic validation
        if (rawHl7 == null || rawHl7.isBlank()) {
            throw new IllegalArgumentException("HL7 message is empty");
        }

        // Step 2: Validate HL7 format BEFORE splitting
        validateHl7FormatBasic(rawHl7);

        // Step 3: Split segments - using simple newline approach (more standard)
//        String[] lines = rawHl7.split("\\r?\\n");
        String[] lines = rawHl7.split("[\\r\\n]+");

        // Step 4: Validate segment structure
        validateHl7Segments(lines);

        String bloodCollectionId = null;
        String instrument = "";
        String sendingFacility = null;
        LocalDateTime messageDateTime = null;
        Integer hl7PatientId = null;
        List<TestResultParameter> testResultParameterList = new ArrayList<>();
        //iF we need PID just add more
        boolean hasMSH = false;
        boolean hasOBR = false;
        boolean hasOBX = false;
        boolean hasPID = false;

        for (String line : lines) {
            if (line == null || line.isBlank()) continue;

            String[] parts = line.split(Pattern.quote("|"));
            if (parts.length == 0) continue;

            String segmentType = parts[0];

            switch (segmentType) {
                case "MSH":
                    hasMSH = true;
                    instrument = parts.length > 2 ? parts[2] : "";

                    // Extract MSH-4 (Sending Facility) for runBy
                    sendingFacility = parts.length > 3 ? parts[3].trim() : null;
                    if (sendingFacility == null || sendingFacility.isBlank()) {
                        throw new IllegalArgumentException("Missing Sending Facility in MSH-4");
                    }

                    // Extract MSH-7 (Message DateTime) for runAt
                    String mshDateTime = parts.length > 6 ? parts[6].trim() : null;
                    if (mshDateTime == null || mshDateTime.isBlank()) {
                        throw new IllegalArgumentException("Missing Message DateTime in MSH-7");
                    }

                    messageDateTime = parseHl7DateTime(mshDateTime);

                    break;

                case "PID":
                    hasPID = true;

                    // Extract PID-3 (Patient ID)
                    if (parts.length > 2 && !parts[3].isBlank()) {
                        String patientIdStr = parts[3].trim();
                        try {
                            hl7PatientId = Integer.parseInt(patientIdStr);
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException(
                                    "HL7's Patient ID is not valid: " + patientIdStr
                            );
                        }
                    } else {
                        throw new IllegalArgumentException("Missing Patient ID in PID-3");
                    }

                    break;

                case "OBR":
                    hasOBR = true;
                    if (parts.length > 2 && !parts[2].isBlank()) {
                        bloodCollectionId = parts[2].trim();
                    } else {
                        throw new IllegalArgumentException(
                                "Missing blood collection ID in OBR-2 (Placer Order Number). " +
                                        "BloodCollectionId is required and cannot be empty."
                        );
                    }
                    break;

                case "OBX":
                    hasOBX = true;
                    if (parts.length < 6) continue;

                    TestResultParameter testResultParameter = new TestResultParameter();
                    testResultParameter.setSequence(safeInt(parts, 1));
                    testResultParameter.setObxIdentifier(parts.length > 3 ? parts[3] : "UNKNOWN");

                    // OBX-3 = "WBC^White Blood Cell"
                    String[] idSplit = parts[3].split("\\^");
                    testResultParameter.setParamCode(idSplit[0]);
                    testResultParameter.setParamName(idSplit.length > 1 ? idSplit[1] : idSplit[0]);

                    testResultParameter.setValue(parts.length > 5 ? parts[5] : null);
                    testResultParameter.setUnit(parts.length > 6 ? parts[6] : null);
                    testResultParameter.setRefRange(parts.length > 7 ? parts[7] : null);
                    testResultParameter.setFlag(parts.length > 8 ? parts[8] : "N");
                    testResultParameter.setComputedBy("HL7 Parser v2.0");

                    testResultParameterList.add(testResultParameter);
                    break;

                default:
                    // Ignore other segment types
                    break;
            }
        }

        // Step 6: Validate required segments exist
        if (!hasMSH) throw new IllegalArgumentException("Invalid HL7 format: Missing MSH segment");
        if (!hasPID) throw new IllegalArgumentException("Invalid HL7 format: Missing PID segment");
        if (!hasOBR) throw new IllegalArgumentException("Invalid HL7 format: Missing OBR segment");
        if (!hasOBX) throw new IllegalArgumentException("Invalid HL7 format: Missing OBX segment");

        if (bloodCollectionId == null)
            throw new IllegalArgumentException("Missing blood collection id number in HL7 message");

        final String finalBloodCollectionId = bloodCollectionId.trim();

        // Step 7: Check if TestResult already exists (early exit for duplicate)
        //If don't want to log, remove log line
        testResultRepository.findByBloodCollectionId(finalBloodCollectionId)
                .ifPresent(r -> {
                    log.warn("TestResult already exists for bloodCollectionId: {}", finalBloodCollectionId);
                    throw new IllegalArgumentException(
                            "TestResult already exists for bloodCollectionId: " + finalBloodCollectionId
                    );
                });

        // Step 8: Find TestOrder
        //If don't want to log, remove the parentheses and log line
        TestOrder order = testOrderRepository.findByBloodCollectionId(finalBloodCollectionId)
                .orElseThrow(() -> {
                    log.error("TestOrder not found for bloodCollectionId: {}", finalBloodCollectionId);
                    return new IllegalArgumentException(
                            "TestOrder not found for bloodCollectionId: " + finalBloodCollectionId
                    );
                });

        // Step 9: Validate Patient ID matches
        if (hl7PatientId == null) {
            throw new IllegalArgumentException("Patient ID from HL7 is null");
        }

        if (!hl7PatientId.equals(order.getPatientId())) {
            throw new IllegalArgumentException(
                    "HL7's Patient ID " + hl7PatientId +
                            " is not match with Patient ID " + order.getPatientId() +
                            " in Test Order"
            );
        }

        // Step 10: Create new TestResult
        log.info("Creating new TestResult for bloodCollectionId: '{}'", finalBloodCollectionId);
        TestResult result = TestResult.builder()
                .testOrder(order)
                .bloodCollectionId(order.getBloodCollectionId())
                .instrumentName(instrument)
                .hl7RawData(rawHl7)
                .status("COMPLETED")
                .build();

        // Step 11: Link parameters to result and order
        for (TestResultParameter p : testResultParameterList) {
            p.setTestResult(result);
            p.setTestOrder(order);
        }
        result.setTestResultParameter(testResultParameterList);
        order.setRunAt(messageDateTime);
        order.setRunBy(sendingFacility);

        // Step 12: Save entities
        TestResult savedResult = testResultRepository.save(result);
        order.setStatus(TestOrderStatus.COMPLETED);
        testOrderRepository.save(order);

        // Step 13: Publish event for event-driven architecture
        eventPublisher.publishTestResultCreated(savedResult);

        // Step 14: Build response
        TestResultResponse testResultResponse = testResultMapper.toTestResultResponse(savedResult);

        return RestResponse.<TestResultResponse>builder()
                .statusCode(200)
                .result(testResultResponse)
                .message("HL7 data processed successfully")
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Validates basic HL7 format structure before parsing
     */
    private void validateHl7FormatBasic(String rawHl7) {
        // Check if message starts with MSH segment
        if (!rawHl7.trim().startsWith("MSH")) {
            throw new IllegalArgumentException("Invalid HL7 format: Message must start with MSH segment");
        }

        // Check for proper segment separators
        if (!rawHl7.contains("\r") && !rawHl7.contains("\n")) {
            throw new IllegalArgumentException("Invalid HL7 format: Missing segment separators");
        }

        // Validate MSH segment structure
        String[] firstLine = rawHl7.split("\\r?\\n")[0].split(Pattern.quote("|"), -1);
        if (firstLine.length < 3) {
            throw new IllegalArgumentException("Invalid HL7 format: MSH segment has insufficient fields");
        }

        // Validate encoding characters in MSH-2
        if (firstLine.length > 1 && firstLine[1].length() < 4) {
            throw new IllegalArgumentException("Invalid HL7 format: Missing encoding characters in MSH-2");
        }
    }

    /**
     * Validates individual segment structure
     */
    private void validateHl7Segments(String[] segments) {
        if (segments == null || segments.length == 0) {
            throw new IllegalArgumentException("Invalid HL7 format: No segments found");
        }

        for (String segment : segments) {
            if (segment.isBlank()) continue;

            // Check for field separator
            if (!segment.contains("|")) {
                throw new IllegalArgumentException(
                        "Invalid HL7 format: Segment missing field separator: " + segment
                );
            }

            // Check segment identifier (3 characters, letters or numbers)
            // Using [A-Z0-9] to support segments like ZMD, FT1, etc.
            if (segment.length() < 3 || !segment.substring(0, 3).matches("[A-Z0-9]{3}")) {
                throw new IllegalArgumentException(
                        "Invalid HL7 format: Invalid segment identifier: " + segment
                );
            }
        }
    }

    /**
     * Safely parse integer from segment parts
     */
    private int safeInt(String[] parts, int index) {
        try {
            return parts.length > index ? Integer.parseInt(parts[index]) : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Parse HL7 DateTime format (YYYYMMDDHHmmss) to LocalDateTime
     * Format: 20251017210049 -> 2025-10-17 21:00:49
     */
    private LocalDateTime parseHl7DateTime(String hl7DateTime) {
        try {
            // HL7 DateTime format: YYYYMMDDHHmmss (14 characters)
            if (hl7DateTime.length() < 14) {
                throw new IllegalArgumentException(
                        "Invalid HL7 DateTime format. Expected YYYYMMDDHHmmss, got: " + hl7DateTime
                );
            }

            int year = Integer.parseInt(hl7DateTime.substring(0, 4));
            int month = Integer.parseInt(hl7DateTime.substring(4, 6));
            int day = Integer.parseInt(hl7DateTime.substring(6, 8));
            int hour = Integer.parseInt(hl7DateTime.substring(8, 10));
            int minute = Integer.parseInt(hl7DateTime.substring(10, 12));
            int second = Integer.parseInt(hl7DateTime.substring(12, 14));

            return LocalDateTime.of(year, month, day, hour, minute, second);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Failed to parse HL7 DateTime: " + hl7DateTime + ". Error: " + e.getMessage()
            );
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RestResponse<Void> republishTestResultEvent(String testOrderId) {
        log.info("Attempting to republish event for testOrderId: {}", testOrderId);
        TestOrder order = testOrderRepository.findById(testOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("TestOrder not found with id: " + testOrderId));
        TestResult testResult = order.getTestResults();
        if (testResult == null) {
            log.warn("No TestResult found for orderId: {}. Cannot republish event.", testOrderId);
            throw new ResourceNotFoundException("No test result found for this order to republish.");
        }
        eventPublisher.publishTestResultCreated(testResult);
        log.info("Successfully triggered republishing of event for orderId: {}", testOrderId);
        return RestResponse.<Void>builder()
                .statusCode(200)
                .message("Successfully triggered event republishing for test order " + testOrderId)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public RestResponse<TestResultResponse> reprocessHl7ByBloodCollectionId(String bloodCollectionId) {
        String originalHl7 = self.deleteAndPrepareForReprocess(bloodCollectionId);
        log.info("Old data for {} deleted. Re-ingesting now in a separate transaction...", bloodCollectionId);
        return self.receiveHl7(originalHl7);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public String deleteAndPrepareForReprocess(String bloodCollectionId) {
        log.warn("Executing DELETION transaction for bloodCollectionId: {}", bloodCollectionId);
        TestResult oldResult = testResultRepository.findByBloodCollectionId(bloodCollectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot re-process. No existing TestResult found for: " + bloodCollectionId));
        String hl7Data = oldResult.getHl7RawData();
        TestOrder order = oldResult.getTestOrder();
        testResultRepository.delete(oldResult);
        if (order != null) {
            order.setStatus(TestOrderStatus.PENDING);
            order.setTestResults(null);
            testOrderRepository.save(order);
        }
        return hl7Data;
    }

    //This method's logic is not suitable for partial HL7 validation
//    private void validateHl7Format(String rawHl7) {
//        String[] lines = rawHl7.trim().split("(?=(PID|OBR|OBX|ZMD|FT1|NTE|ORC))");
//        validateHl7Format(lines);
//    }
}