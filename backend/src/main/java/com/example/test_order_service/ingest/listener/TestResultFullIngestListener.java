package com.example.test_order_service.ingest.listener;

import com.example.test_order_service.ingest.dto.TestResultIngestFullPayload;
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

    @KafkaListener(
            topics = "test-results-full",
            groupId = "test-result-full-ingest-group",
            containerFactory = "kafkaIngestListenerContainerFactory"
    )
    public void onMessage(@Valid TestResultIngestFullPayload payload) {
        try {
            log.info("Ingest listener received payload: orderId={} resultId={}",
                    payload.getTestOrderId(), payload.getResultId());
            ingestService.ingest(payload);
        } catch (Exception ex) {
            log.error("Failed to ingest payload for orderId={} resultId={}",
                    payload == null ? "null" : payload.getTestOrderId(),
                    payload == null ? "null" : payload.getResultId(), ex);
            throw ex;
        }
    }
}