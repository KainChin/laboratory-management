package com.example.test_order_service.ingest.publisher;

import com.example.test_order_service.entity.Comment;
import com.example.test_order_service.entity.TestOrder;
import com.example.test_order_service.ingest.dto.CommentEventPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CommentEventPublisher {

    // Đặt tên topic cho các sự kiện comment
    private static final String COMMENT_EVENTS_TOPIC = "comment-events-topic";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Gửi sự kiện comment (tạo, cập nhật, v.v.)
     *
     * @param comment   Đối tượng Comment đã được lưu
     * @param eventType Loại sự kiện (ví dụ: "COMMENT_CREATED")
     */
    public void publishCommentEvent(Comment comment, String eventType) {

        TestOrder testOrder = comment.getTestOrder();
        if (comment == null || comment.getTestOrder() == null) {
            log.warn("Attempted to publish comment event for a null comment or null testOrder. Aborting.");
            return;
        }

        String testOrderId = comment.getTestOrder().getTestOrderId();

        try {
            // 1. Xây dựng payload
            CommentEventPayload payload = CommentEventPayload.builder()
                    .eventType(eventType)
                    .commentId(comment.getCommentId())
                    .testOrderId(testOrderId)
                    .patientId(testOrder.getPatientId()) // <-- LẤY PATIENT ID
                    .email(testOrder.getEmail())
                    .commentText(comment.getCommentText())
                    .createdBy(comment.getCreatedBy())
                    .createdAt(comment.getCreatedAt())
                    .build();

            // 2. Gửi sự kiện
            // Chúng ta dùng testOrderId làm key để đảm bảo các comment của cùng 1 order
            // sẽ đi vào cùng 1 partition (nếu topic có nhiều partition)
            kafkaTemplate.send(COMMENT_EVENTS_TOPIC, testOrderId, payload)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to send comment event for orderId='{}', commentId='{}'.",
                                    testOrderId, comment.getCommentId(), ex);
                        } else {
                            log.info("Successfully sent comment event '{}' for commentId='{}'. Topic: {}",
                                    eventType,
                                    comment.getCommentId(),
                                    result.getRecordMetadata().topic());
                        }
                    });

        } catch (Exception e) {
            log.error("An unexpected error occurred while building or sending comment event for orderId='{}'.",
                    testOrderId, e);
        }
    }
}