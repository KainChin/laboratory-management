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
        if (rawHl7 == null || rawHl7.isBlank()) {
            throw new IllegalArgumentException("HL7 message is empty");
        }
        String[] lines = rawHl7.trim().split("(?=(PID|OBR|OBX|ZMD|FT1|NTE|ORC))");

        validateHl7Format(lines);

        String bloodCollectionId = null;
        String instrument = "";
        List<TestResultParameter> testResultParameterList = new ArrayList<>();

        boolean hasMSH = false;
        boolean hasOBR = false;
        boolean hasOBX = false;

        for (String line : lines) {
            if (line == null || line.isBlank()) continue;
            String[] parts = line.split(Pattern.quote("|"));

            if (parts.length == 0) continue;

            String segmentType = parts[0];

            switch (segmentType) {
                case "MSH":
                    hasMSH = true;
                    instrument = parts.length > 2 ? parts[2] : "";
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
                    break;
            }
        }

        if (!hasMSH) throw new IllegalArgumentException("Invalid HL7 format: Missing MSH segment");
        if (!hasOBR) throw new IllegalArgumentException("Invalid HL7 format: Missing OBR segment");
        if (!hasOBX) throw new IllegalArgumentException("Invalid HL7 format: Missing OBX segment");

        if (bloodCollectionId == null) {
            throw new IllegalArgumentException("Missing blood collection id number in HL7 message");
        }

        final String finalBloodCollectionId = bloodCollectionId.trim();

        TestOrder order = testOrderRepository.findByBloodCollectionId(finalBloodCollectionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "TestOrder not found for blood collection Id: " + finalBloodCollectionId));

        Optional<TestResult> existingResultOpt = testResultRepository.findByBloodCollectionId(finalBloodCollectionId);

        TestResult result;
        if (existingResultOpt.isPresent()) {
            log.warn("TestResult for '{}' already exists. Updating it.", finalBloodCollectionId);
            result = existingResultOpt.get();

            result.getTestResultParameter().clear();

            result.setInstrumentName(instrument);
            result.setHl7RawData(rawHl7);
            result.setStatus("UPDATED");

            for (TestResultParameter p : testResultParameterList) {
                p.setTestResult(result);
                p.setTestOrder(order);
                result.getTestResultParameter().add(p);
            }
        } else {
            log.info("Creating new TestResult for '{}'.", finalBloodCollectionId);
            result = TestResult.builder()
                    .testOrder(order)
                    .bloodCollectionId(order.getBloodCollectionId())
                    .instrumentName(instrument)
                    .hl7RawData(rawHl7)
                    .status("COMPLETED")
                    .build();

            for (TestResultParameter p : testResultParameterList) {
                p.setTestResult(result);
                p.setTestOrder(order);
            }
            result.setTestResultParameter(testResultParameterList);
        }

        TestResult savedResult = testResultRepository.save(result);
        order.setStatus(TestOrderStatus.COMPLETED);
        testOrderRepository.save(order);
        eventPublisher.publishTestResultCreated(savedResult);
        TestResultResponse testResultResponse = testResultMapper.toTestResultResponse(savedResult);

        return RestResponse.<TestResultResponse>builder()
                .statusCode(200)
                .result(testResultResponse)
                .message("HL7 data processed successfully (created or updated).")
                .timestamp(LocalDateTime.now())
                .build();
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

    private void validateHl7Format(String[] segments) {
        if (segments == null || segments.length == 0 || !segments[0].startsWith("MSH")) {
            throw new IllegalArgumentException("Invalid HL7 format: Message must start with MSH segment");
        }
        String[] firstLineParts = segments[0].split(Pattern.quote("|"), -1);
        if (firstLineParts.length < 3) {
            throw new IllegalArgumentException("Invalid HL7 format: MSH segment has insufficient fields");
        }
        if (firstLineParts.length > 1 && firstLineParts[1].length() < 4) {
            throw new IllegalArgumentException("Invalid HL7 format: Missing encoding characters in MSH-2");
        }
        for (String segment : segments) {
            if (segment.isBlank()) continue;
            if (!segment.contains("|")) {
                throw new IllegalArgumentException("Invalid HL7 format: Segment missing field separator: " + segment);
            }
            if (segment.length() < 3 || !segment.substring(0, 3).matches("[A-Z0-9]{3}")) {
                throw new IllegalArgumentException("Invalid HL7 format: Invalid segment identifier: " + segment);
            }
        }
    }

    private void validateHl7Format(String rawHl7) {
        String[] lines = rawHl7.trim().split("(?=(PID|OBR|OBX|ZMD|FT1|NTE|ORC))");
        validateHl7Format(lines);
    }

    private int safeInt(String[] parts, int index) {
        try {
            return parts.length > index ? Integer.parseInt(parts[index]) : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}