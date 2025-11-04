package com.example.test_order_service.ingest.service;

import com.example.test_order_service.dto.response.RestResponse;
import com.example.test_order_service.dto.response.TestResultResponse;
import com.example.test_order_service.entity.TestOrder;
import com.example.test_order_service.entity.TestResult;
import com.example.test_order_service.entity.TestResultParameter;
import com.example.test_order_service.entity.enumForEntity.TestOrderStatus;
import com.example.test_order_service.ingest.publisher.TestResultEventPublisher; // <-- THÊM IMPORT NÀY
import com.example.test_order_service.mapper.TestResultMapper;
import com.example.test_order_service.repository.TestOrderRepository;
import com.example.test_order_service.repository.TestResultRepository;
import com.example.test_order_service.service.TestResultService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Primary
public class TestResultServiceKafka implements TestResultService {
    private final TestResultRepository testResultRepository;
    private final TestOrderRepository testOrderRepository;
    private final TestResultMapper testResultMapper;
    private final TestResultEventPublisher eventPublisher;

    @Transactional
    public RestResponse<?> getResultByBloodCollectionId(String bloodCollectionId) {
        TestResult testResult = testResultRepository.findByBloodCollectionId(bloodCollectionId)
                .orElseThrow(() -> new IllegalArgumentException("Test result not found for blood collection Id: " + bloodCollectionId));

        TestResultResponse testResultResponse = testResultMapper.toTestResultResponse(testResult);

        return RestResponse.<TestResultResponse>builder()
                .statusCode(200)
                .result(testResultResponse)
                .message("Retrived test result successfully")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Override
    public RestResponse<TestResultResponse> receiveHl7(String rawHl7) {
        if (rawHl7 == null || rawHl7.isBlank()) {
            throw new IllegalArgumentException("HL7 message is empty");
        }

        String[] lines = rawHl7.split("\\r?\\n");
        String bloodCollectionId = null;
        String instrument = "";
        List<TestResultParameter> testResultParameterList = new ArrayList<>();

        for (String line : lines) {
            if (line == null || line.isBlank()) continue;
            String[] parts = line.split("\\|");

            if (parts.length == 0) continue;

            if (parts[0].equals("MSH")) {
                instrument = parts.length > 2 ? parts[2] : "";
            }

            if (parts[0].equals("OBR")) {
                bloodCollectionId = parts.length > 2 && !parts[2].isBlank() ? parts[2] :
                        (parts.length > 3 ? parts[3] : null);
            } else if (parts[0].equals("OBX")) {
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
            }
        }

        if (bloodCollectionId == null) {
            throw new IllegalArgumentException("Missing blood collection id number in HL7 message");
        }

        final String finalBloodCollectionId = bloodCollectionId.trim();

        TestOrder order = testOrderRepository.findByBloodCollectionId(finalBloodCollectionId).orElseThrow(() -> new IllegalArgumentException("TestOrder not found for blood collection Id: " + finalBloodCollectionId));

        TestResult result = TestResult.builder()
                .testOrder(order)
//                .patientId(order.getPatientId())
                .bloodCollectionId(order.getBloodCollectionId())
                .instrumentName(instrument)
                .hl7RawData(rawHl7)
                .status("COMPLETE")
                .build();

        for (TestResultParameter p : testResultParameterList) {
            p.setTestResult(result);
            p.setTestOrder(order);
        }

        result.setTestResultParameter(testResultParameterList);

        TestResult savedResult = testResultRepository.save(result); // <-- Lưu vào biến savedResult

        order.setStatus(TestOrderStatus.COMPLETED);
        testOrderRepository.save(order);

        // <-- GỌI PUBLISHER SAU KHI LƯU DB THÀNH CÔNG
        eventPublisher.publishTestResultCreated(savedResult);

        TestResultResponse testResultResponse = testResultMapper.toTestResultResponse(savedResult); // <-- Dùng biến savedResult

        return RestResponse.<TestResultResponse>builder()
                .statusCode(200)
                .result(testResultResponse)
                .message("HL7 parsing successfully")
                .timestamp(LocalDateTime.now())
                .build();
    }

    private int safeInt(String[] parts, int index) {
        try {
            return Integer.parseInt(parts[index]);
        } catch (Exception e) {
            return 0;
        }
    }
}