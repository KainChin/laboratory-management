package com.example.test_order_service.dto.response;

import com.example.test_order_service.entity.enumForEntity.Gender;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientDto {
    private Integer patientId;
    private String fullName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    private Gender gender;
    private String phone;
    private String email;
    private String address;
    private String identityNumber;
    private String createdBy;
    private String updatedBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Boolean isActive;
}

