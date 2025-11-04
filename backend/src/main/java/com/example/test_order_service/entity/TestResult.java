package com.example.test_order_service.entity;

import com.example.test_order_service.base.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "test_results")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestResult extends BaseEntity {
    @Id
    @Column(name = "result_id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String resultId;

    @OneToOne
    @JoinColumn(name = "test_order_id", nullable = false)
    private TestOrder testOrder;

    @Column(name = "patient_id")
    private String patientId;

    @Column(name = "blood_collection_id", nullable = false)
    private String bloodCollectionId;

    @Column(name = "instrument_name", length = 100)
    private String instrumentName;

    @Column(name = "hl7_raw_data", columnDefinition = "TEXT", nullable = false)
    private String hl7RawData;

    @Column(name = "status", length = 30)
    private String status; // COMPLETED / AI_REVIEW / REVIEWED / REJECTED

    @OneToMany(mappedBy = "testResult", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TestResultParameter> testResultParameter;
}