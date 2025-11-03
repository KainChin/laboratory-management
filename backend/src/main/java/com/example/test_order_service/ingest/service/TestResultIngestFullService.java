package com.example.test_order_service.ingest.service;

import com.example.test_order_service.entity.TestOrder;
import com.example.test_order_service.entity.TestResult;
import com.example.test_order_service.entity.enumForEntity.TestResultStatus;
import com.example.test_order_service.exception.ResourceNotFoundException;
import com.example.test_order_service.ingest.dto.TestResultIngestFullPayload;
import com.example.test_order_service.repository.TestOrderRepository;
import com.example.test_order_service.repository.TestResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestResultIngestFullService {
    private final TestResultRepository testResultRepository; // existing repo
    private final TestOrderRepository testOrderRepository;   // existing repo

    /**
     * Ingest or upsert a full TestResult payload.
     * - If payload.resultId provided and found -> update that record
     * - Else create new TestResult (JPA will generate resultId)
     */
    @Transactional
    public void ingest(TestResultIngestFullPayload p) {
        if (p == null) {
            log.warn("Received null payload");
            return;
        }

        String orderId = p.getTestOrderId();
        TestOrder order = testOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("TestOrder not found: " + orderId));

        // Try to update by resultId if provided
        Optional<TestResult> existingOpt = Optional.empty();
        if (p.getResultId() != null && !p.getResultId().isBlank()) {
            existingOpt = testResultRepository.findById(p.getResultId());
        }

        if (existingOpt.isPresent()) {
            TestResult tr = existingOpt.get();
            log.debug("Updating existing TestResult id={} for orderId={}", tr.getResultId(), orderId);

            // Update fields from payload if non-null
            if (p.getParameter() != null) tr.setParameter(p.getParameter());
            if (p.getValue() != null) tr.setValue(p.getValue());
            if (p.getUnit() != null) tr.setUnit(p.getUnit());
            tr.setMinValue(p.getMinValue());
            tr.setMaxValue(p.getMaxValue());
            if (p.getFlag() != null) tr.setFlag(p.getFlag());
            if (p.getStatus() != null) {
                try {
                    tr.setStatus(TestResultStatus.valueOf(p.getStatus()));
                } catch (IllegalArgumentException ex) {
                    log.warn("Unknown status '{}', ignoring", p.getStatus());
                }
            }
            if (p.getCreatedBy() != null) tr.setCreatedBy(p.getCreatedBy());
            if (p.getCreatedAt() != null) tr.setCreatedAt(p.getCreatedAt());
            tr.setUpdatedAt(p.getUpdatedAt() == null ? LocalDateTime.now() : p.getUpdatedAt());

            testResultRepository.save(tr);
            log.info("Updated TestResult id={} for orderId={}", tr.getResultId(), orderId);
            return;
        }

        // No existing by resultId -> create new
        TestResult trNew = TestResult.builder()
                // do NOT set resultId if you want JPA to generate it; but if upstream provided one you may set it (we avoid to not conflict)
                .testOrder(order)
                .parameter(p.getParameter())
                .value(p.getValue())
                .unit(p.getUnit())
                .minValue(p.getMinValue())
                .maxValue(p.getMaxValue())
                .flag(p.getFlag())
                .status(p.getStatus() == null ? TestResultStatus.COMPLETED : parseStatus(p.getStatus()))
                .createdBy(p.getCreatedBy() == null ? "external-system" : p.getCreatedBy())
                .createdAt(p.getCreatedAt() == null ? LocalDateTime.now() : p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();

        testResultRepository.save(trNew);
        log.info("Inserted new TestResult id={} for orderId={}", trNew.getResultId(), orderId);
    }

    private TestResultStatus parseStatus(String s) {
        try {
            return TestResultStatus.valueOf(s);
        } catch (Exception ex) {
            log.warn("Cannot parse status '{}', defaulting to COMPLETED", s);
            return TestResultStatus.COMPLETED;
        }
    }
}