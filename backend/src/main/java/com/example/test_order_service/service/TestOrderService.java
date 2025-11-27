package com.example.test_order_service.service;

import com.example.test_order_service.dto.response.PageResponse;
import com.example.test_order_service.dto.response.RestResponse;
import com.example.test_order_service.dto.response.TestOrderDetailResponse;
import com.example.test_order_service.dto.response.TestOrderResponse;
import com.example.test_order_service.dto.request.TestOrderRequest;
import com.example.test_order_service.dto.request.TestOrderUpdateRequest;
import com.example.test_order_service.entity.enumForEntity.TestOrderStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public interface TestOrderService {
    RestResponse<TestOrderResponse> createTestOrder(TestOrderRequest request, String authToken);
    RestResponse<TestOrderResponse> updateTestOrder(String orderId, TestOrderUpdateRequest request);
    PageResponse<TestOrderResponse> getTestOrders(Pageable pageable, String keyword, LocalDate startDate, LocalDate endDate, TestOrderStatus status);
    RestResponse<TestOrderDetailResponse> getTestOrderById(String orderId);
    PageResponse<TestOrderResponse> deleteTestOrder(String orderId, Pageable pageable, String keyword, LocalDate startDate, LocalDate endDate, TestOrderStatus status);
    RestResponse<?> getTestOrderStatistics();
    RestResponse<TestOrderResponse> reviewTestOrder(String orderId);
    PageResponse<TestOrderDetailResponse> getTestOrderByEmail(Pageable pageable, String email);
    RestResponse<?> getDailyStatistics();
    RestResponse<TestOrderResponse> getTestOrderByBloodCollectionId(String bloodCollectionId);
    RestResponse<Void> resyncTestOrderResults(String orderId);
}
