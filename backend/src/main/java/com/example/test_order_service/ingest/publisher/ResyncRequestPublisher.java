package com.example.test_order_service.ingest.publisher;

import com.example.test_order_service.ingest.dto.ResyncRequestPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResyncRequestPublisher {

    private static final String RESYNC_REQUEST_TOPIC = "test-result-resync-request-topic";
    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Sửa lại phương thức để nhận thêm bloodCollectionId
     */
    public void requestResync(String testOrderId, String bloodCollectionId, String reason) {
        if (testOrderId == null || testOrderId.isBlank()) {
            log.warn("Attempted to request resync for a null or empty testOrderId. Aborting.");
            return;
        }

        ResyncRequestPayload payload = ResyncRequestPayload.builder()
                .testOrderId(testOrderId)
                .bloodCollectionId(bloodCollectionId) // <-- Gán bloodCollectionId vào payload
                .reason(reason)
                .build();

        try {
            kafkaTemplate.send(RESYNC_REQUEST_TOPIC, testOrderId, payload)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to send resync request for testOrderId='{}' to Kafka.", testOrderId, ex);
                        } else {
                            log.info("Successfully sent resync request for testOrderId='{}', bloodCollectionId='{}'. Topic: {}, Partition: {}",
                                    testOrderId,
                                    bloodCollectionId,
                                    result.getRecordMetadata().topic(),
                                    result.getRecordMetadata().partition());
                        }
                    });
        } catch (Exception e) {
            log.error("An unexpected error occurred while sending resync request for testOrderId='{}'.", testOrderId, e);
        }
    }
}