package com.example.test_order_service.dto.request;

import com.example.test_order_service.entity.TestOrder;
import com.example.test_order_service.entity.enumForEntity.TestResultStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public class TestResultRequest {
    @NotBlank(message = "Parameter is required")
    private String parameter;

    @NotBlank(message = "Value is required")
    private Double value;
}
