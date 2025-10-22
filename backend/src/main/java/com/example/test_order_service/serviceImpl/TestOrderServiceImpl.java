package com.example.test_order_service.serviceImpl;

import com.example.test_order_service.dto.repsonse.PageResponse;
import com.example.test_order_service.dto.repsonse.RestResponse;
import com.example.test_order_service.dto.repsonse.TestOrderResponse;
import com.example.test_order_service.dto.request.TestOrderRequest;
import com.example.test_order_service.dto.request.TestOrderUpdateRequest;
import com.example.test_order_service.entity.TestOrder;
import com.example.test_order_service.exception.ResourceNotFoundException;
import com.example.test_order_service.mapper.TestOrderMapper;
import com.example.test_order_service.repository.TestOrderRepository;
import com.example.test_order_service.service.TestOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class TestOrderServiceImpl implements TestOrderService {

    private final TestOrderRepository testOrderRepository;
    private final TestOrderMapper testOrderMapper;

    @Override
    public RestResponse<TestOrderResponse> createTestOrder(TestOrderRequest request) {
        TestOrder testOrder = testOrderMapper.toTestOrderEntity(request);
        testOrder.setCreatedBy("System");

        TestOrderResponse response = testOrderMapper.toTestOrderResponse(testOrderRepository.save(testOrder));

        return RestResponse.<TestOrderResponse>builder()
                .statusCode(200)
                .result(response)
                .message("Test order created successfully")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Override
    public PageResponse<TestOrderResponse> getTestOrders(Pageable pageable, String keyword) {
        Page<TestOrder> testOrderPage = testOrderRepository.findTestOrdersByParams(pageable, keyword);

        return PageResponse.<TestOrderResponse>builder()
                .currentPage(testOrderPage.getNumber() + 1)
                .totalPages(testOrderPage.getTotalPages())
                .items(testOrderPage.stream()
                        .map(testOrderMapper::toTestOrderResponse)
                        .toList())
                .build();
    }

    @Override
    public RestResponse<TestOrderResponse> updateTestOrder(String orderId, TestOrderUpdateRequest request) {
        TestOrder testOrder = testOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Test order not found"));
        testOrder.setCitizenId(request.getCitizenId() != null ? request.getCitizenId() : testOrder.getCitizenId());
        testOrder.setPatientName(
                request.getPatientName() != null ? request.getPatientName() : testOrder.getPatientName());
        testOrder.setDateOfBirth(
                request.getDateOfBirth() != null ? request.getDateOfBirth() : testOrder.getDateOfBirth());
        testOrder.setCountry(request.getCountry() != null ? request.getCountry() : testOrder.getCountry());
        testOrder.setGender(request.getGender() != null ? request.getGender() : testOrder.getGender());
        testOrder.setStatus(request.getStatus() != null ? request.getStatus() : testOrder.getStatus());
        testOrder.setPhone(request.getPhone() != null ? request.getPhone() : testOrder.getPhone());
        testOrder.setAddress(request.getAddress() != null ? request.getAddress() : testOrder.getAddress());
        testOrder.setEmail(request.getEmail() != null ? request.getEmail() : testOrder.getEmail());

        TestOrderResponse response = testOrderMapper.toTestOrderResponse(testOrderRepository.save(testOrder));

        return RestResponse.<TestOrderResponse>builder()
                .statusCode(200)
                .result(response)
                .message("Test order updated successfully")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Override
    public PageResponse<TestOrderResponse> deleteTestOrder(String orderId, Pageable pageable, String keyword) {
        TestOrder testOrder = testOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Test order not found"));

        testOrder.setDeleted(true);
        testOrderRepository.save(testOrder);

        return getTestOrders(pageable, keyword);
    }

    @Override
    public RestResponse<?> getTestOrderStatistics() {
        long total = testOrderRepository.count();
        var groupedCounts = testOrderRepository.countByStatus();

        long pending = 0;
        long completed = 0;
        long cancelled = 0;

        for (Object[] row : groupedCounts) {
            String status = row[0].toString();
            long count = (long) row[1];
            switch (status) {
                case "PENDING" -> pending = count;
                case "COMPLETED" -> completed = count;
                case "CANCELLED" -> cancelled = count;
            }
        }

        var result = new java.util.HashMap<String, Long>();
        result.put("total", total);
        result.put("pending", pending);
        result.put("completed", completed);
        result.put("cancelled", cancelled);

        return RestResponse.builder()
                .statusCode(200)
                .message("Statistics retrieved successfully")
                .result(result)
                .timestamp(java.time.LocalDateTime.now())
                .build();
    }

}
