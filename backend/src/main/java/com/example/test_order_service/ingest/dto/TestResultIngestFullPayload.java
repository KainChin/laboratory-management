package com.example.test_order_service.ingest.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Full payload for ingest. Matches fields in entity TestResult (except external_result_id).
 * Upstream can send resultId (optional) to request update of that TestResult.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestResultIngestFullPayload {
    // Optional: if upstream provides DB id (UUID), we will try to update that record
    private String resultId;

    @NotBlank
    private String testOrderId;

    @NotBlank
    private String parameter;

    private Double value;
    private String unit;
    private Double minValue;
    private Double maxValue;
    private Boolean flag;
    private String status; // enum name, e.g. "COMPLETED"

    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}