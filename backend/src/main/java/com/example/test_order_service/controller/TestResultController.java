package com.example.test_order_service.controller;

import com.example.test_order_service.dto.repsonse.RestResponse;
import com.example.test_order_service.dto.request.TestResultRequest;
import com.example.test_order_service.entity.TestResult;
import com.example.test_order_service.service.TestResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/test-orders/{orderId}/results")
@RequiredArgsConstructor
public class TestResultController {
    private final TestResultService testResultService;

    @PostMapping
    public RestResponse<Void> createTestResult(@PathVariable String orderId, @RequestBody List<TestResultRequest> request) {
        return null;
    }
}
