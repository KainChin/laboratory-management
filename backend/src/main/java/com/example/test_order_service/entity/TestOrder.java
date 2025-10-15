package com.example.test_order_service.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Entity
@Data
@Table(name = "test_orders")
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
    @Temporal(TemporalType.DATE)
    private Date dateOfBirth;

    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDate createdAt = LocalDate.now();

    @Column(name = "run_by")
    private String runBy;

    @Column(name = "run_at")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDate runAt;

    @Column(name = "reviewed_by")
    private String reviewedBy;

    @Column(name = "reviewed_at")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDate reviewedAt;

    @OneToMany(mappedBy = "testOrder", cascade = CascadeType.ALL)
    private List<TestResult> testResults;

    @OneToMany(mappedBy = "testOrder", cascade = CascadeType.ALL)
    private List<Comment> comments;
}