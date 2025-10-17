package com.example.test_order_service.entity;

import com.example.test_order_service.entity.enumForEntity.Gender;
import com.example.test_order_service.entity.enumForEntity.ResultStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table(name = "test_orders")
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class TestOrder {
    @Id
    @Column(name = "test_order_id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String testOrderId;

    @Column(name = "patient_id")
    private String patientId;

    @Column(name = "patient_name", nullable = false, length = 150)
    private String patientName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "citizen_id", nullable = false, length = 30)
    private String citizenId;

    @Column(name = "country", nullable = false, length = 20)
    private String country;

    @Column(name = "gender", length = 10)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "status", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private ResultStatus status;

    @Column(name = "created_by", nullable = false, updatable = false)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "run_by")
    private String runBy;

    @Column(name = "run_at")
    private LocalDateTime runAt;

    @Column(name = "reviewed_by")
    private String reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @OneToMany(mappedBy = "testOrder", cascade = CascadeType.ALL)
    private List<TestResult> testResults;

    @OneToMany(mappedBy = "testOrder", cascade = CascadeType.ALL)
    private List<Comment> comments;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.status = ResultStatus.PENDING;
    }
}