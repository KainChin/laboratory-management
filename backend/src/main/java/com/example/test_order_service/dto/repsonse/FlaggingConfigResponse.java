package com.example.test_order_service.dto.repsonse;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlaggingConfigResponse {
    private String configId;
    private String parameter;
    private Double minValue;
    private Double maxValue;
    private String unit;
}
