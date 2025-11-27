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
        @NotNull(message = "Patient ID is required")
    private Integer patientId;

    @NotBlank(message = "Patient name is required")
    private String patientName;

    @JsonFormat(pattern = "dd/MM/yyyy")
    @PastOrPresent(message = "Date of birth cannot be in the future")
    private LocalDate dateOfBirth;

    @AssertTrue(message = "Date of birth cannot be more than 120 years ago")
    private boolean isValidDateOfBirth() {
        if (dateOfBirth == null) {
            return true;
        }
        LocalDate minDate = LocalDate.now().minusYears(120);
        return dateOfBirth.isAfter(minDate);
    }

    @NotBlank(message = "Identity number is required")
    @Size(max = 30, message = "Identity number must not exceed 30 characters")
    private String identityNumber;

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
