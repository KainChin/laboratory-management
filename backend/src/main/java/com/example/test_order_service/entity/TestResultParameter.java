package com.example.test_order_service.entity;

import com.example.test_order_service.base.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "test_result_parameters")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestResultParameter extends BaseEntity {
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
}
