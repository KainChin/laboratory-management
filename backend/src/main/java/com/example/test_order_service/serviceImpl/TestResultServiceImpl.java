package com.example.test_order_service.serviceImpl;

import com.example.test_order_service.dto.response.RestResponse;
import com.example.test_order_service.dto.response.TestResultResponse;
import com.example.test_order_service.entity.TestOrder;
import com.example.test_order_service.entity.TestResult;
import com.example.test_order_service.entity.TestResultParameter;
import com.example.test_order_service.entity.enumForEntity.TestOrderStatus;
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
        // Step 1: Basic validation
        if (rawHl7 == null || rawHl7.isBlank()) {
            throw new IllegalArgumentException("HL7 message is empty");
        }

        // Step 2: Validate HL7 format
        validateHl7Format(rawHl7);

        String[] lines = rawHl7.split("\\r?\\n");
        String bloodCollectionId = null;
        String instrument = "";
        List<TestResultParameter> testResultParameterList = new ArrayList<>();

        //iF we need PID just add more
        boolean hasMSH = false;
        boolean hasOBR = false;
        boolean hasOBX = false;

        for (String line : lines) {
            if (line == null || line.isBlank()) continue;
            String[] parts = line.split("\\|");

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

        // Step 3: Check that required segments exist
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

        testResultRepository.findByBloodCollectionId(finalBloodCollectionId)
                .ifPresent(r -> {
                    throw new IllegalArgumentException("TestResult already exists for bloodCollectionId: " + finalBloodCollectionId);
                });

        TestOrder order = testOrderRepository.findByBloodCollectionId(finalBloodCollectionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "TestOrder not found for blood collection Id: " + finalBloodCollectionId));

        TestResult result = TestResult.builder()
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

        testResultRepository.save(result);

        order.setStatus(TestOrderStatus.COMPLETED);
        testOrderRepository.save(order);

        TestResultResponse testResultResponse = testResultMapper.toTestResultResponse(result);

        return RestResponse.<TestResultResponse>builder()
                .statusCode(200)
                .result(testResultResponse)
                .message("HL7 parsing successfully")
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Validates basic HL7 format structure
     * According to section 3.6.1.3 - Publish Test Results Using HL7
     */
    private void validateHl7Format(String rawHl7) {
        // Check if message starts with MSH segment
        if (!rawHl7.trim().startsWith("MSH")) {
            throw new IllegalArgumentException("Invalid HL7 format: Message must start with MSH segment");
        }

        // Check for proper field separator (|)
        String[] firstLine = rawHl7.split("\\r?\\n")[0].split("\\|", -1);
        if (firstLine.length < 3) {
            throw new IllegalArgumentException("Invalid HL7 format: MSH segment has insufficient fields");
        }

        // Validate encoding characters in MSH-2
        if (firstLine.length > 1 && firstLine[1].length() < 4) {
            throw new IllegalArgumentException("Invalid HL7 format: Missing encoding characters in MSH-2");
        }

        // Check for at least one segment separator (newline)
        if (!rawHl7.contains("\r") && !rawHl7.contains("\n")) {
            throw new IllegalArgumentException("Invalid HL7 format: Missing segment separators");
        }

        // Validate segment structure - each line should have pipe delimiters
        String[] segments = rawHl7.split("\\r?\\n");
        for (String segment : segments) {
            if (segment.isBlank()) continue;

            if (!segment.contains("|")) {
                throw new IllegalArgumentException("Invalid HL7 format: Segment missing field separator: " + segment);
            }

            // Check if segment starts with valid 3-character segment ID
            if (segment.length() < 3 || !segment.substring(0, 3).matches("[A-Z]{3}")) {
                throw new IllegalArgumentException("Invalid HL7 format: Invalid segment identifier: " + segment);
            }
        }
    }

    //Safely parse integer from segment parts
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
