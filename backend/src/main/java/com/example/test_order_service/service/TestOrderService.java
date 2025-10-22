package com.example.test_order_service.service;

import com.example.test_order_service.dto.repsonse.PageResponse;
import com.example.test_order_service.dto.repsonse.RestResponse;
import com.example.test_order_service.dto.repsonse.TestOrderResponse;
import com.example.test_order_service.dto.request.TestOrderRequest;
import com.example.test_order_service.dto.request.TestOrderUpdateRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public interface TestOrderService {
    RestResponse<TestOrderResponse> createTestOrder(TestOrderRequest request);
    RestResponse<TestOrderResponse> updateTestOrder(String orderId, TestOrderUpdateRequest request);
    PageResponse<TestOrderResponse> getTestOrders(Pageable pageable, String keyword);
    PageResponse<TestOrderResponse> deleteTestOrder(String orderId, Pageable pageable, String keyword);
    RestResponse<?> getTestOrderStatistics();
}
