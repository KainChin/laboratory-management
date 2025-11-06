package com.example.test_order_service.ingest.listener;

import com.example.test_order_service.ingest.service.TestResultServiceKafka;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TestResultFullIngestListener {
    // THAY ĐỔI 1: Inject trực tiếp TestResultServiceKafka
    private final TestResultServiceKafka testResultServiceKafka;

    /**
     * Method này sẽ tự động được gọi mỗi khi có message mới trên topic "instrument-results-topic".
     * Giờ đây nó sẽ nhận một chuỗi String HL7 thô.
     * @param hl7Data Message từ Kafka dưới dạng String.
     */
    @KafkaListener(
            topics = "instrument-results-topic",
            groupId = "test-order-ingest-group"
            // Bỏ containerFactory nếu nó được cấu hình riêng cho JSON.
            // Nếu không, hãy đảm bảo factory đó dùng StringDeserializer.
    )
    public void onMessage(String hl7Data) { // <<<--- THAY ĐỔI 2: Tham số là String
        if (hl7Data == null || hl7Data.isBlank()) {
            log.warn("Kafka listener received an empty message. Skipping.");
            return;
        }

        try {
            log.info("Kafka listener received a raw HL7 message. Delegating to TestResultServiceKafka for processing.");

            // THAY ĐỔI 3: Gọi đến service xử lý HL7
            testResultServiceKafka.receiveHl7(hl7Data);

        } catch (Exception ex) {
            log.error("Failed to process raw HL7 payload from Kafka. Error: {}", ex.getMessage(), ex);
            // Ném lại lỗi để Kafka có thể thực hiện retry theo cấu hình
            throw ex;
        }
    }
}