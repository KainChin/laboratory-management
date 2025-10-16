package com.example.test_order_service.dto.repsonse;

import com.example.test_order_service.entity.ResultStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class TestOrderResponse {
    private String testOrderId;
    private String patientName;
    private LocalDate dateOfBirth;
    private int age;
    private String gender;
    private String address;
    private String email;
    private String phone;
    private ResultStatus status;
}
