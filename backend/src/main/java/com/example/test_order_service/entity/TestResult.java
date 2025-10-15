package com.example.test_order_service.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "test_results")
public class TestResult {
    @Id
    @Column(name = "result_id")
    private String resultId;

    @ManyToOne
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
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    // Getters and setters...
}