package com.example.test_order_service.ingest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * DTO này khớp với cấu trúc JSON đầy đủ được gửi từ instrument-service qua Kafka.
 * @JsonIgnoreProperties(ignoreUnknown = true) giúp bỏ qua các trường lạ trong JSON mà không gây lỗi.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class InstrumentResultPayload {

    private String testOrderId;
    private String patientId;
    private OffsetDateTime analyzedAt;
    private List<ResultItem> results;
    private Metadata metadata;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ResultItem {
        private String parameter;
        private String value;
        private String referenceRange;
        private boolean flagged;
        private String unit;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Metadata {
        private String instrumentId;
        private String technician;
    }
}
