package com.example.test_order_service.ingest.listener;

import com.example.test_order_service.ingest.dto.InstrumentResultPayload;
import com.example.test_order_service.ingest.service.TestResultIngestFullService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TestResultFullIngestListener {
    private final TestResultIngestFullService ingestService;

    /**
     * Method này sẽ tự động được gọi mỗi khi có message mới trên topic "instrument-results-topic".
     * @param payload Message từ Kafka đã được tự động chuyển thành đối tượng InstrumentResultPayload.
     */
    @KafkaListener(
            topics = "instrument-results-topic",
            groupId = "test-order-ingest-group",
            containerFactory = "kafkaIngestListenerContainerFactory"
    )
    public void onMessage(@Valid InstrumentResultPayload payload) {
        try {
            log.info("Kafka listener received payload for testOrderId={}", payload.getTestOrderId());
            ingestService.ingestFullPayload(payload);
        } catch (Exception ex) {
            log.error("Failed to process ingested payload for testOrderId={}. Error: {}",
                    payload != null ? payload.getTestOrderId() : "null", ex.getMessage());
            // Ném lại lỗi để Kafka có thể thực hiện retry theo cấu hình
            throw ex;
        }
    }
}