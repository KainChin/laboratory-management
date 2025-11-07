package com.example.test_order_service.ingest.listener;

// THAY ĐỔI 1: Đổi import service để dùng Interface thay vì class cụ thể (Good practice)
import com.example.test_order_service.service.TestResultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TestResultFullIngestListener {
    // Dùng interface TestResultService
    private final TestResultService testResultService;

    @KafkaListener(
            topics = "instrument-results-topic",
            groupId = "test-order-ingest-group"
    )
    public void onMessage(String hl7Data) {
        if (hl7Data == null || hl7Data.isBlank()) {
            log.warn("Kafka listener received an empty or null message. Skipping.");
            return;
        }

        // THAY ĐỔI 2: SỬA LẠI HOÀN TOÀN KHỐI TRY-CATCH
        try {
            log.info("Kafka listener received a raw message. Delegating to service for processing...");

            // Gọi đến service để xử lý
            testResultService.receiveHl7(hl7Data);

            log.info("Successfully processed message from Kafka.");

        } catch (IllegalArgumentException e) {
            // Bắt các lỗi nghiệp vụ có thể lường trước như "đã tồn tại", "định dạng sai".
            // Ghi log ở mức WARN vì đây là lỗi có thể dự đoán, không phải lỗi hệ thống.
            log.warn("--- SKIPPING MESSAGE ---");
            log.warn("Skipping message due to a business rule violation: {}", e.getMessage());
            log.warn("This is expected if the message is a duplicate or invalid. Payload: [{}]", hl7Data);

            // Quan trọng nhất: KHÔNG NÉM LẠI LỖI.
            // Khi phương thức kết thúc êm đẹp, Kafka sẽ không gửi lại message này.

        } catch (Exception e) {
            // Bắt tất cả các lỗi không mong muốn khác (VD: mất kết nối DB).
            log.error("--- UNEXPECTED ERROR ---");
            log.error("An unexpected error occurred while processing Kafka message. This message will be SKIPPED to prevent blocking.", e);
            log.error("Problematic Payload: [{}]", hl7Data);

            // Tương tự, không ném lại lỗi để tránh làm tắc nghẽn toàn bộ consumer.
        }
    }
}