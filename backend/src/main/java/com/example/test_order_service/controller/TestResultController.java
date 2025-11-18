package com.example.test_order_service.controller;

import com.example.test_order_service.dto.response.RestResponse;
import com.example.test_order_service.dto.response.TestResultResponse;
import com.example.test_order_service.service.TestResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test-results")
@RequiredArgsConstructor
public class TestResultController {
    private final TestResultService testResultService;

    @PostMapping(value = "/hl7", consumes = MediaType.TEXT_PLAIN_VALUE)
    @PreAuthorize("hasAnyRole('LAB_USER', 'ADMIN')")
    public RestResponse<TestResultResponse> receiveHl7(@RequestBody String hl7RawData) {
        return testResultService.receiveHl7(hl7RawData);
    }

    @GetMapping("/{bloodCollectionId}")
    @PreAuthorize("hasAnyRole('LAB_USER', 'ADMIN') and hasAuthority('REVIEW_TEST_ORDER')")
    public RestResponse<?> getResultByBloodCollectionId(@PathVariable String bloodCollectionId) {
        return testResultService.getResultByBloodCollectionId(bloodCollectionId);
    }
}
