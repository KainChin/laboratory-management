package com.example.test_order_service.dto.repsonse;

import com.example.test_order_service.entity.TestOrder;
import com.example.test_order_service.entity.enumForEntity.ResultFlag;
import com.example.test_order_service.entity.enumForEntity.TestResultStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
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
    private Double referenceMin;
    private Double referenceMax;
    private ResultFlag flag;
    private TestResultStatus status;
    private String createdBy;
    private LocalDateTime createdAt;
}
