package com.example.test_order_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "test_results")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestResult {
    @Id
    @Column(name = "result_id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String resultId;

    @ManyToOne
    @JoinColumn(name = "test_order_id", nullable = false)
    private TestOrder testOrder;

//   @Column(name = "patient_id", nullable = false)
//    private String patientId;

    //blood collection tube -> UUID
    @Column(name = "blood_collection_id", nullable = false)
    private String bloodCollectionId;

    @Column(name = "instrument_name", length = 100)
    private String instrumentName;

    @Lob
    @Column(name = "hl7_raw_data", nullable = false)
    private String hl7RawData;

    @Column(name = "status", length = 30)
    private String status; // COMPLETE / AI_REVIEW / REVIEWED / REJECTED

    @OneToMany(mappedBy = "testResult", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TestResultParameter> testResultParameter;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}