package com.example.test_order_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestOrderRequest{
        private String patientName;
        private LocalDate dateOfBirth;
        private String gender;
        private String address;
        private String email;
        private String phone;
}
