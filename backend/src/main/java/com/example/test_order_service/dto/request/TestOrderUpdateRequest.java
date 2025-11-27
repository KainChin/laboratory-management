package com.example.test_order_service.dto.request;

import com.example.test_order_service.entity.enumForEntity.Gender;
import com.example.test_order_service.entity.enumForEntity.TestOrderStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
public class TestOrderUpdateRequest {
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

    @Size(max = 30, message = "Citizen ID must not exceed 30 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\s-]+$", message = "Citizen ID must contain only letters, numbers, spaces and hyphens")
    private String citizenId;
    private String country;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    private TestOrderStatus status;

    @Pattern(regexp = "^(\\+\\d{1,3}[- ]?)?\\d{10}$", message = "Phone number is invalid")
    private String phone;
    private String address;

    @Email(message = "Email should be valid")
    private String email;
}
