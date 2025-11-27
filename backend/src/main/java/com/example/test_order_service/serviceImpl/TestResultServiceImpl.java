package com.example.test_order_service.serviceImpl;

import com.example.test_order_service.dto.response.RestResponse;
import com.example.test_order_service.dto.response.TestResultResponse;
import com.example.test_order_service.entity.TestOrder;
import com.example.test_order_service.entity.TestResult;
import com.example.test_order_service.entity.TestResultParameter;
import com.example.test_order_service.entity.enumForEntity.TestOrderStatus;
import com.example.test_order_service.exception.ResourceNotFoundException;
import com.example.test_order_service.mapper.TestResultMapper;
import com.example.test_order_service.repository.TestOrderRepository;
import com.example.test_order_service.repository.TestResultRepository;
import com.example.test_order_service.service.TestResultService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class TestResultServiceImpl implements TestResultService {
    private final TestResultRepository testResultRepository;
    private final TestOrderRepository testOrderRepository;
    private final TestResultMapper testResultMapper;

    @Transactional
    public RestResponse<?> getResultByBloodCollectionId(String bloodCollectionId) {
        TestResult testResult = testResultRepository.findByBloodCollectionId(bloodCollectionId)
                .orElseThrow(() -> new IllegalArgumentException("Test result not found for blood collection Id: " + bloodCollectionId));

        TestResultResponse testResultResponse = testResultMapper.toTestResultResponse(testResult);

        return RestResponse.<TestResultResponse>builder()
                .statusCode(200)
                .result(testResultResponse)
                .message("Test result retrivived successfully")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Override
    public RestResponse<TestResultResponse> receiveHl7(String rawHl7) {
        // Setp 1: Basic validation
        if (rawHl7 == null || rawHl7.isBlank()) {
            throw new IllegalArgumentException("HL7 message is empty");
        }

        // Step 2: Validate HL7 format BEFORE splitting
        validateHl7FormatBasic(rawHl7);

        // Step 3: Split segments - using simple newline approach (more standard)
        String[] lines = rawHl7.split("\\r?\\n");

        // Step 4: Validate segment structure
        validateHl7Segments(lines);

        String bloodCollectionId = null;
        String instrument = "";
        List<TestResultParameter> testResultParameterList = new ArrayList<>();
        //iF we need PID just add more
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
                    // Ignore other segment types (PID, etc.)
                    break;
            }
        }

        // Step 6: Validate required segments exist
        if (!hasMSH) {
            throw new IllegalArgumentException("Invalid HL7 format: Missing MSH segment");
        }
        if (!hasOBR) {
            throw new IllegalArgumentException("Invalid HL7 format: Missing OBR segment");
        }
        if (!hasOBX) {
            throw new IllegalArgumentException("Invalid HL7 format: Missing OBX segment");
        }

        if (bloodCollectionId == null) {
            throw new IllegalArgumentException("Missing blood collection id number in HL7 message");
        }

        final String finalBloodCollectionId = bloodCollectionId.trim();

        // Step 7: Check if TestResult already exists (early exit for duplicate)
        testResultRepository.findByBloodCollectionId(finalBloodCollectionId)
                .ifPresent(r -> {
                    throw new IllegalArgumentException(
                            "TestResult already exists for bloodCollectionId: " + finalBloodCollectionId
                    );
                });

        // Step 8: Find TestOrder
        TestOrder order = testOrderRepository.findByBloodCollectionId(finalBloodCollectionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "TestOrder not found for blood collection Id: " + finalBloodCollectionId
                ));
        if (order.isDeleted()) {
            throw new ResourceNotFoundException("Test order not found");
        }

        // Step 9: Create new TestResult
        TestResult result = TestResult.builder()
                .testOrder(order)
                .bloodCollectionId(order.getBloodCollectionId())
                .instrumentName(instrument)
                .hl7RawData(rawHl7)
                .status("COMPLETED")
                .build();

        // Step 10: Link parameters to result and order
        for (TestResultParameter p : testResultParameterList) {
            p.setTestResult(result);
            p.setTestOrder(order);
        }
        result.setTestResultParameter(testResultParameterList);

        // Step 11: Save entities
        TestResult savedResult = testResultRepository.save(result);
        order.setStatus(TestOrderStatus.COMPLETED);
        testOrderRepository.save(order);

        // Step 12: Build response
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

    @Override
    public RestResponse<Void> republishTestResultEvent(String testOrderId) {
        // Vì class này không xử lý Kafka, chúng ta sẽ throw lỗi hoặc trả về thông báo không hỗ trợ.
        // Trả về thông báo lỗi sẽ thân thiện với người dùng hơn.
        return RestResponse.<Void>builder()
                .statusCode(501) // 501 Not Implemented
                .message("Republishing events is not supported in this service implementation.")
                .error("Not Implemented")
                .timestamp(LocalDateTime.now())
                .build();
    }
}
