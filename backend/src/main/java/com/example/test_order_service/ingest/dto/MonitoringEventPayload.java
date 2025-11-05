package com.example.test_order_service.ingest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * DTO này được tạo ra để khớp với cấu trúc EventLog mà Monitoring Service mong đợi.
 * Đây là "Data Contract" giữa test-order-service và monitoring-service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonitoringEventPayload {

    @JsonProperty("eventId")
    private String eventId;

    @JsonProperty("logType")
    private String logType; // "EVENT" hoặc "AUDIT"

    @JsonProperty("action")
    private String action; // VD: "TEST_RESULT_CREATED"

    @JsonProperty("serviceName")
    private String serviceName; // Tên service của bạn: "test-order-service"

    @JsonProperty("operatorId")
    private String operatorId; // Người thực hiện, nếu có

    @JsonProperty("operatorName")
    private String operatorName; // Tên người thực hiện, nếu có

    @JsonProperty("correlationId")
    private String correlationId;

    @JsonProperty("severity")
    private String severity; // "INFO", "WARN", "ERROR"

    @JsonProperty("eventTimestamp")
    private Instant eventTimestamp;

    @JsonProperty("createdAt")
    private Instant createdAt;

    @JsonProperty("expireAt")
    private Instant expireAt; // Dùng để tự động xóa log trong MongoDB

    @JsonProperty("payload")
    private Map<String, Object> payload; // Dữ liệu chi tiết của event

    @JsonProperty("tags")
    private List<String> tags;

    @JsonProperty("raw")
    private String raw; // Dữ liệu gốc nếu cần
}