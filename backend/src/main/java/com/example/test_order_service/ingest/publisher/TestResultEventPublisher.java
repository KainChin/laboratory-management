package com.example.test_order_service.ingest.publisher;

import com.example.test_order_service.entity.TestResult;
import com.example.test_order_service.ingest.dto.TestResultCreatedEvent;
import com.example.test_order_service.ingest.dto.TestResultParameterEventPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class TestResultEventPublisher {

    private static final String TOPIC = "test-results-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishTestResultCreated(TestResult testResult) {
        if (testResult == null) {
            log.warn("Attempted to publish a null TestResult. Aborting.");
            return;
        }

        try {
            TestResultCreatedEvent event = buildEventFromEntity(testResult);

            kafkaTemplate.send(TOPIC, event.getBloodCollectionId(), event)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to send TestResultCreatedEvent for bloodCollectionId='{}' to Kafka.",
                                    event.getBloodCollectionId(), ex);
                        } else {
                            log.info("Successfully sent TestResultCreatedEvent for bloodCollectionId='{}'. Topic: {}, Partition: {}",
                                    event.getBloodCollectionId(),
                                    result.getRecordMetadata().topic(),
                                    result.getRecordMetadata().partition());
                        }
                    });

        } catch (Exception e) {
            log.error("An unexpected error occurred while building or sending TestResultCreatedEvent for orderId='{}'.",
                    testResult.getTestOrder().getTestOrderId(), e);
        }
    }

    private TestResultCreatedEvent buildEventFromEntity(TestResult testResult) {
        return TestResultCreatedEvent.builder()
                .testOrderId(testResult.getTestOrder().getTestOrderId())
                .bloodCollectionId(testResult.getBloodCollectionId())
                .instrumentName(testResult.getInstrumentName())
                .status(testResult.getStatus())
                .parameters(testResult.getTestResultParameter().stream()
                        .map(param -> TestResultParameterEventPayload.builder()
                                .paramCode(param.getParamCode())
                                .paramName(param.getParamName())
                                .value(param.getValue())
                                .unit(param.getUnit())
                                .refRange(param.getRefRange())
                                .flag(param.getFlag())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}