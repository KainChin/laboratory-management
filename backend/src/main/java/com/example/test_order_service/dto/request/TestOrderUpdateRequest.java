package com.example.test_order_service.dto.request;

import com.example.test_order_service.entity.enumForEntity.Gender;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.util.regex.Pattern;

@Data
public class TestOrderUpdateRequest {
    private String citizenId;
    private String patientName;

    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dateOfBirth;

    private Integer age;
    private Gender gender;
    private String phone;
    private String address;
    private String email;
}
