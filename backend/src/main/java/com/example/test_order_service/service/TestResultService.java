package com.example.test_order_service.service;

import com.example.test_order_service.dto.response.RestResponse;
import org.springframework.stereotype.Service;

@Service
public interface TestResultService {
//    RestResponse<Void> createTestResult(String orderId, List<TestResultRequest> request);
    RestResponse<?> receiveHl7(String hl7RawData);
    RestResponse<?> getResultByBloodCollectionId(String bloodCollectionId);
}
