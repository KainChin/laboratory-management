package com.example.test_order_service.serviceImpl;

import com.example.test_order_service.dto.repsonse.RestResponse;
import com.example.test_order_service.dto.request.TestResultRequest;
import com.example.test_order_service.repository.TestResultRepository;
import com.example.test_order_service.service.TestResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TestResultServiceImpl implements TestResultService {
    private final TestResultRepository testResultRepository;

    @Override
    public RestResponse<Void> createTestResult(String orderId, List<TestResultRequest> request) {
        return null;
    }
}
