package com.example.test_order_service.service;

import com.example.test_order_service.dto.repsonse.RestResponse;
import com.example.test_order_service.dto.repsonse.TestOrderResponse;
import com.example.test_order_service.dto.request.TestOrderRequest;

public interface TestOrderService {
    RestResponse<TestOrderResponse> createTestOrder(TestOrderRequest request);
}
