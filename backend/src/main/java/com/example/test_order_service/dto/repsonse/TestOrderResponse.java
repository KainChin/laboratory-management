package com.example.test_order_service.dto.repsonse;

import com.example.test_order_service.entity.enumForEntity.ResultStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestOrderResponse {
    private String testOrderId;
    private String patientName;

    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dateOfBirth;
    private String citizenId;
    private String country;
    private int age;
    private String gender;
    private String address;
    private String email;
    private String phone;
    private ResultStatus status;
}
