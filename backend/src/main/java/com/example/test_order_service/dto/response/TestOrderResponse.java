package com.example.test_order_service.dto.response;

import com.example.test_order_service.entity.enumForEntity.Gender;
import com.example.test_order_service.entity.enumForEntity.TestOrderStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
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
    private String patientId;

    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dateOfBirth;
    private String citizenId;
    private String country;
    private Gender gender;
    private String address;
    private String email;
    private String phone;
    private String bloodCollectionId;
    private String createdBy;
    private TestOrderStatus status;
}
