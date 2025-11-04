package com.example.test_order_service.dto.request;

import com.example.test_order_service.entity.TestOrder;
import com.example.test_order_service.entity.TestResultParameter;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.List;

public class TestResultRequest {
    @NotBlank(message = "HL7 Raw Data is required")
    private String hl7RawData;
}