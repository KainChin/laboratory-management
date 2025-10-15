package com.example.test_order_service.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Data
@Table(name = "flagging_configs")
public class FlaggingConfig {
    @Id
    @Column(name = "config_id")
    private String configId;

    @Column(name = "parameter", nullable = false, length = 50)
    private String parameter;

    @Column(name = "min_value", precision = 10, scale = 2)
    private Double minValue;

    @Column(name = "max_value", precision = 10, scale = 2)
    private Double maxValue;

    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "updated_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
}