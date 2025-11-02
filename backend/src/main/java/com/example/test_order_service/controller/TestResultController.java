package com.example.test_order_service.controller;

import com.example.test_order_service.dto.repsonse.RestResponse;
import com.example.test_order_service.dto.request.TestResultRequest;
import com.example.test_order_service.entity.TestResult;
import com.example.test_order_service.service.TestResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/test-results")
@RequiredArgsConstructor
public class TestResultController {
    private final TestResultService testResultService;

    @PostMapping(value = "/hl7", consumes = MediaType.TEXT_PLAIN_VALUE)
    public RestResponse<?> receiveHl7(@RequestBody String hl7RawData) {
        return testResultService.receiveHl7(hl7RawData);
    }

    @GetMapping("/{bloodCollectionId}")
    public RestResponse<?> getResultByBloodCollectionId(@PathVariable String bloodCollectionId) {
        return testResultService.getResultByBloodCollectionId(bloodCollectionId);
    }
}
