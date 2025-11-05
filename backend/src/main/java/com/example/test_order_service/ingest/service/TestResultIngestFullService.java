package com.example.test_order_service.ingest.service;

import com.example.test_order_service.entity.TestOrder;
import com.example.test_order_service.entity.TestResult;
import com.example.test_order_service.entity.TestResultParameter;
import com.example.test_order_service.entity.enumForEntity.TestOrderStatus;
import com.example.test_order_service.exception.ResourceNotFoundException;
import com.example.test_order_service.ingest.dto.InstrumentResultPayload;
import com.example.test_order_service.repository.TestOrderRepository;
import com.example.test_order_service.repository.TestResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestResultIngestFullService {
    private final TestResultRepository testResultRepository;
    private final TestOrderRepository testOrderRepository;

    @Transactional
    public void ingestFullPayload(InstrumentResultPayload payload) {
        String orderId = payload.getTestOrderId();
        log.info("Starting ingestion process for testOrderId: {}", orderId);

        // 1. Tìm TestOrder tương ứng trong DB
        TestOrder order = testOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot ingest result. TestOrder not found for ID: " + orderId));

        // 2. Kiểm tra dữ liệu: patientId phải khớp nhau để đảm bảo an toàn
        if (payload.getPatientId() != null && !Objects.equals(order.getPatientId(), payload.getPatientId())) {
            log.error("Data integrity violation for orderId='{}': Payload patientId='{}' does not match DB patientId='{}'. Aborting ingestion.",
                    orderId, payload.getPatientId(), order.getPatientId());
            throw new IllegalArgumentException("Patient ID mismatch.");
        }

        // 3. Kiểm tra xem kết quả đã được xử lý chưa
        if (testResultRepository.findByTestOrder(order).isPresent()) {
            log.warn("TestResult already exists for orderId: {}. Skipping ingestion to prevent duplication.", orderId);
            return;
        }

        // 4. Tạo đối tượng TestResult (cha)
        TestResult testResult = createTestResultFromPayload(payload, order);

        // 5. Tạo danh sách các TestResultParameter (con)
        List<TestResultParameter> parameters = createParametersFromPayload(payload, testResult, order);
        testResult.setTestResultParameter(parameters);

        // 6. Cập nhật trạng thái của TestOrder và liên kết hai chiều
        updateTestOrderStatus(order, testResult);

        // 7. Lưu vào DB. Do có CascadeType.ALL, cả cha và con sẽ được lưu
        testResultRepository.save(testResult);

        log.info("Successfully ingested and saved TestResult with {} parameters for orderId: {}", parameters.size(), orderId);
    }

    private TestResult createTestResultFromPayload(InstrumentResultPayload payload, TestOrder order) {
        InstrumentResultPayload.Metadata metadata = payload.getMetadata();
        TestResult testResult = TestResult.builder()
                .testOrder(order)
                .patientId(order.getPatientId())
                .bloodCollectionId(order.getBloodCollectionId())
                .instrumentName(metadata != null ? metadata.getInstrumentId() : "Unknown")
                .hl7RawData("N/A - Ingested from Kafka as JSON") // Ghi chú lại nguồn dữ liệu
                .status("COMPLETED")
                .build();
        testResult.setCreatedBy(metadata != null ? metadata.getTechnician() : "KafkaIngest");
        testResult.setCreatedAt(payload.getAnalyzedAt() != null ? payload.getAnalyzedAt().toLocalDateTime() : LocalDateTime.now());
        return testResult;
    }

    private List<TestResultParameter> createParametersFromPayload(InstrumentResultPayload payload, TestResult testResult, TestOrder order) {
        List<TestResultParameter> parameters = new ArrayList<>();
        int sequence = 1;
        for (InstrumentResultPayload.ResultItem item : payload.getResults()) {
            TestResultParameter parameter = TestResultParameter.builder()
                    .testResult(testResult)
                    .testOrder(order)
                    .sequence(sequence++)
                    .paramCode(item.getParameter())
                    .paramName(item.getParameter())
                    .value(item.getValue())
                    .unit(item.getUnit())
                    .refRange(item.getReferenceRange())
                    .flag(item.isFlagged() ? "Abnormal" : "Normal") // Chuyển boolean -> chuỗi có ý nghĩa hơn
                    .computedBy("instrument-service")
                    .build();
            parameter.setCreatedAt(testResult.getCreatedAt());
            parameter.setCreatedBy(testResult.getCreatedBy());
            parameters.add(parameter);
        }
        return parameters;
    }

    private void updateTestOrderStatus(TestOrder order, TestResult testResult) {
        order.setStatus(TestOrderStatus.COMPLETED);
        order.setRunAt(testResult.getCreatedAt());
        order.setRunBy(testResult.getCreatedBy());
        order.setTestResults(testResult); // Liên kết hai chiều
    }
}