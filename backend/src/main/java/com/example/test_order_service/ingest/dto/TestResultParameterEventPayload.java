package com.example.test_order_service.ingest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO chứa thông tin của một parameter trong kết quả xét nghiệm, dùng cho Kafka event.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestResultParameterEventPayload {
    private String paramCode;
    private String paramName;
    private String value;
    private String unit;
    private String refRange;
    private String flag;
}