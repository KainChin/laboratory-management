package com.example.test_order_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    @JsonBackReference
    @JoinColumn(name = "test_order_id", nullable = false)
    private TestOrder testOrder;

    @Column(name = "parameter", nullable = false, length = 50)
    private String parameter;

    @Column(name = "value", nullable = false, length = 50)
    private String value;

    @Column(name = "reference_range", length = 50)
    private String referenceRange;

    @Column(name = "flagged")
    private Boolean flagged;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}