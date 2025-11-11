package com.example.test_order_service.ingest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Payload được gửi đi để yêu cầu một service khác gửi lại dữ liệu.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResyncRequestPayload {

    @Builder.Default
    private String requestId = UUID.randomUUID().toString();


    /**
     * ID của TestOrder cần được đồng bộ lại.
     */
    private String testOrderId;
    private  String bloodCollectionId;
    /**
     * Service nào đang gửi yêu cầu này.
     */
    @Builder.Default
    private String requestedByService = "test-order-service";

    /**
     * Thời điểm gửi yêu cầu.
     */
    @Builder.Default
    private Instant requestTimestamp = Instant.now();

    /**
     * Lý do yêu cầu đồng bộ (tùy chọn, để debug).
     * Ví dụ: "ManualTriggerByUser", "MissingDataCorrection"
     */
    private String reason;
}