package com.example.test_order_service.ingest.publisher;

import com.example.test_order_service.entity.TestOrder;
import com.example.test_order_service.entity.TestResult;
import com.example.test_order_service.ingest.dto.MonitoringEventPayload;
import com.example.test_order_service.ingest.dto.TestResultParameterEventPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class TestResultEventPublisher {

    private static final String TOPIC = "test-results-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publishTestResultCreated(TestResult testResult) {
        if (testResult == null) {
            log.warn("Attempted to publish a null TestResult. Aborting.");
            return;
        }

        try {
            MonitoringEventPayload event = buildMonitoringEvent(testResult);

            kafkaTemplate.send(TOPIC, event.getCorrelationId(), event)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to send MonitoringEventPayload for bloodCollectionId='{}' to Kafka.",
                                    testResult.getBloodCollectionId(), ex);
                        } else {
                            log.info("Successfully sent MonitoringEventPayload for bloodCollectionId='{}'. Topic: {}, Partition: {}",
                                    testResult.getBloodCollectionId(),
                                    result.getRecordMetadata().topic(),
                                    result.getRecordMetadata().partition());
                        }
                    });

        } catch (Exception e) {
            log.error("An unexpected error occurred while building or sending MonitoringEventPayload for orderId='{}'.",
                    // Thêm kiểm tra null ở đây để log an toàn hơn
                    (testResult.getTestOrder() != null ? testResult.getTestOrder().getTestOrderId() : "UNKNOWN"), e);
        }
    }

    private MonitoringEventPayload buildMonitoringEvent(TestResult testResult) {
        Instant now = Instant.now();

        // Lấy đối tượng TestOrder một cách an toàn
        TestOrder testOrder = testResult.getTestOrder();
        if (testOrder == null) {
            // Ném lỗi rõ ràng thay vì để xảy ra NullPointerException
            throw new IllegalStateException("Cannot build event. TestOrder is null in the given TestResult.");
        }

        // ---- SỬA LỖI: Dùng HashMap thay vì Map.of để cho phép giá trị null ----
        // Hoặc kiểm tra và gán giá trị mặc định cho từng trường.
        Map<String, Object> detailedPayload = new HashMap<>();
        detailedPayload.put("testOrderId", testOrder.getTestOrderId());
        detailedPayload.put("patientId", testResult.getPatientId());
        detailedPayload.put("bloodCollectionId", testResult.getBloodCollectionId());
        detailedPayload.put("instrumentName", testResult.getInstrumentName() != null ? testResult.getInstrumentName() : "Unknown");
        detailedPayload.put("status", testResult.getStatus() != null ? testResult.getStatus() : "Unknown");

        // Xử lý danh sách parameters một cách an toàn
        List<TestResultParameterEventPayload> parameterPayloads;
        if (testResult.getTestResultParameter() != null) {
            parameterPayloads = testResult.getTestResultParameter().stream()
                    .map(param -> TestResultParameterEventPayload.builder()
                            .paramCode(param.getParamCode())
                            .paramName(param.getParamName())
                            .value(param.getValue())
                            .unit(param.getUnit())
                            .refRange(param.getRefRange())
                            .flag(param.getFlag())
                            .build())
                    .collect(Collectors.toList());
        } else {
            parameterPayloads = Collections.emptyList(); // Trả về danh sách rỗng nếu bị null
        }
        detailedPayload.put("parameters", parameterPayloads);


        return MonitoringEventPayload.builder()
                .eventId(UUID.randomUUID().toString())
                .logType("EVENT")
                .action("TEST_RESULT_CREATED")
                .serviceName("test-order-service")
                .correlationId(testResult.getBloodCollectionId())
                .severity("INFO")
                .eventTimestamp(testResult.getCreatedAt() != null ? testResult.getCreatedAt().toInstant(java.time.ZoneOffset.UTC) : now)
                .createdAt(now)
                .expireAt(now.plus(90, ChronoUnit.DAYS))
                .payload(detailedPayload) // <-- Đã được xử lý an toàn
                .tags(List.of("test-result", "ingestion", testResult.getInstrumentName() != null ? testResult.getInstrumentName() : "Unknown"))
                .operatorId(testResult.getCreatedBy())
                .build();
    }
}