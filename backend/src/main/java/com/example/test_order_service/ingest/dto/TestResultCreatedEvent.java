package com.example.test_order_service.ingest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Event được publish khi một TestResult mới được tạo và hoàn thành.
 * Gửi đi thông tin đầy đủ của kết quả và các thông số.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestResultCreatedEvent {
    // --- Metadata của Event ---
    @Builder.Default
    private String eventId = UUID.randomUUID().toString();

    @Builder.Default
    private String eventType = "TEST_RESULT_CREATED";

    @Builder.Default
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp = LocalDateTime.now();

    @Builder.Default
    private String sourceService = "test-order-service";

    // --- Dữ liệu Payload ---
    private String testOrderId;
    private String bloodCollectionId;
    private String instrumentName;
    private String status; // e.g., "COMPLETED"

    private List<TestResultParameterEventPayload> parameters;
}