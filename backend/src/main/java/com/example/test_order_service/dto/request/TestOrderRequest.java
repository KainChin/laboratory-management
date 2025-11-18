package com.example.test_order_service.dto.request;

import com.example.test_order_service.entity.enumForEntity.Gender;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestOrderRequest {
    @NotBlank(message = "Patient name is required")
    private String patientName;

    @JsonFormat(pattern = "dd/MM/yyyy")
    @PastOrPresent(message = "Date of birth cannot be in the future")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Citizen ID is required")
    private String citizenId;

    @NotBlank(message = "Country is required")
    private String country;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String address;

    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^(\\+\\d{1,3}[- ]?)?\\d{10}$", message = "Phone number is invalid")
    private String phone;
}
