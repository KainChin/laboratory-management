package com.example.test_order_service.ingest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentEventPayload {

    @Builder.Default
    private String eventId = UUID.randomUUID().toString();

    /**
     * Loại sự kiện: "COMMENT_CREATED", "COMMENT_UPDATED", "COMMENT_DELETED"
     */
    private String eventType;

    @Builder.Default
    private String sourceService = "test-order-service";

    @Builder.Default
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventTimestamp = LocalDateTime.now();

    // --- Dữ liệu của Comment ---
    private String commentId;
    private String testOrderId;
    private String email;
    private String commentText;
    private String createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}