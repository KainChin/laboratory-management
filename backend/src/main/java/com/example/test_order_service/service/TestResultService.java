package com.example.test_order_service.service;

import com.example.test_order_service.dto.repsonse.RestResponse;
import com.example.test_order_service.dto.repsonse.TestOrderResponse;
import com.example.test_order_service.dto.request.TestResultRequest;
import com.example.test_order_service.entity.TestResult;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Service
public interface TestResultService {
//    RestResponse<Void> createTestResult(String orderId, List<TestResultRequest> request);
    RestResponse<?> receiveHl7(String hl7RawData);
    RestResponse<?> getResultByBloodCollectionId(String bloodCollectionId);
}
