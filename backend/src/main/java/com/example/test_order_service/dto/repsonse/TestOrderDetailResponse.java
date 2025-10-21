package com.example.test_order_service.dto.repsonse;

import com.example.test_order_service.entity.enumForEntity.Gender;
import com.example.test_order_service.entity.enumForEntity.ResultStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class TestOrderDetailResponse {
    private String testOrderId;
    private String patientId;
    private String patientName;
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dateOfBirth;
    private Integer age;
    private String citizenId;
    private String country;
    private Gender gender;
    private String phone;
    private String address;
    private String email;
    private ResultStatus status;
    private String createdBy;
    private LocalDateTime createdAt = LocalDateTime.now();
    private String runBy;
    private LocalDateTime runAt;
    private String reviewedBy;
    private LocalDateTime reviewedAt;
//
//    @OneToMany(mappedBy = "testOrder", cascade = CascadeType.ALL)
//    private List<TestResult> testResults;
//
//    @OneToMany(mappedBy = "testOrder", cascade = CascadeType.ALL)
//    private List<Comment> comments;
}
