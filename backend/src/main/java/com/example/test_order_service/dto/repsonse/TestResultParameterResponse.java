package com.example.test_order_service.dto.repsonse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TestResultParameterResponse {
    private Integer sequence;
    private String paramCode;
    private String paramName;
    private String value;
    private String unit;
    private String refRange;
    private String flag;
}
