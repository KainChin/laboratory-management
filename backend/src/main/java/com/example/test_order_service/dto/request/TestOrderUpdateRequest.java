package com.example.test_order_service.dto.request;

import com.example.test_order_service.entity.enumForEntity.Gender;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TestOrderUpdateRequest {
    private String patientName;
    private LocalDate dateOfBirth;
    private Integer age;
    private Gender gender;
    private String phone;
    private String address;
    private String email;
}
