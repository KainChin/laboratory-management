package com.example.test_order_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "test_result_parameters")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestResultParameter {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "test_result_id", nullable = false)
    private TestResult testResult;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "test_order_id", nullable = false)
    private TestOrder testOrder;

    //if instrucment provides raw HL7 OBX-3 ID
//    private String rawHl7Id;
    private Integer sequence;
    private String obxIdentifier;
    private String paramCode;
    private String paramName;
    private String value;
    private String unit;
    private String refRange;
    private String flag;          // N / H / L
    private String computedBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
