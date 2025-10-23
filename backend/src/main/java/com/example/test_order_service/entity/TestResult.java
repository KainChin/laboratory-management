package com.example.test_order_service.entity;

import com.example.test_order_service.entity.enumForEntity.ResultFlag;
import com.example.test_order_service.entity.enumForEntity.TestOrderStatus;
import com.example.test_order_service.entity.enumForEntity.TestResultStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonBackReference;

import java.time.LocalDateTime;

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
    @JsonBackReference
    private TestOrder testOrder;

    @Column(name = "parameter", nullable = false, length = 50)
    private String parameter;

    @Column(name = "value", nullable = false, length = 50)
    private Double value;

    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "reference_min")
    private Double referenceMin;

    @Column(name = "reference_max")
    private Double referenceMax;

    @Enumerated(EnumType.STRING)
    @Column(name = "flag", length = 30)
    private ResultFlag flag;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30)
    private TestResultStatus status;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.status = TestResultStatus.PENDING;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}