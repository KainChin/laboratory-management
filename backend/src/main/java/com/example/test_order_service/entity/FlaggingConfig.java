package com.example.test_order_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "flagging_configs")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlaggingConfig {
    @Id
    @Column(name = "config_id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String configId;

    @Column(name = "parameter", nullable = false, length = 50)
    private String parameter;

    @Column(name = "min_value")
    private Double minValue;

    @Column(name = "max_value")
    private Double maxValue;

    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}