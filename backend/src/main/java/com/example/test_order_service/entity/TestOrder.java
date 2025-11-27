package com.example.test_order_service.entity;

import com.example.test_order_service.base.BaseEntity;
import com.example.test_order_service.entity.enumForEntity.Gender;
import com.example.test_order_service.entity.enumForEntity.TestOrderStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "test_orders")
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class TestOrder extends BaseEntity {
    @Id
    @Column(name = "test_order_id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String testOrderId;

    @Column(name = "patient_id")
    private Integer patientId;

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

    //blood collection tube -> UUID
    @Column(name = "blood_collection_id", nullable = false)
    private String bloodCollectionId;

    @Column(name = "status", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private TestOrderStatus status;

    @Column(name = "run_by")
    private String runBy;

    @Column(name = "run_at")
    private LocalDateTime runAt;

    @Column(name = "reviewed_by")
    private String reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @OneToOne(mappedBy = "testOrder", cascade = CascadeType.ALL)
    @JsonIgnore
    private TestResult testResults;

    @OneToMany(mappedBy = "testOrder", cascade = CascadeType.ALL)
    private List<Comment> comments;

    @Override
    protected void onCreate() {
        super.onCreate();
        this.status = TestOrderStatus.PENDING;
    }
}