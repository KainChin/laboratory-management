package com.example.test_order_service.dto.repsonse;

import com.example.test_order_service.entity.enumForEntity.TestResultStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class TestResultResponse {
    private String resultId;
    private String parameter;
    private Double value;
    private String unit;
    private Double minValue;
    private Double maxValue;
    private Boolean flag;
    private TestResultStatus status;
    private String createdBy;
    private LocalDateTime createdAt;
}
