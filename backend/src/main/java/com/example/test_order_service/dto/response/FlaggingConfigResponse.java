package com.example.test_order_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlaggingConfigResponse {
    private String configId;
    private String parameter;
    private Double minValue;
    private Double maxValue;
    private String unit;
}
