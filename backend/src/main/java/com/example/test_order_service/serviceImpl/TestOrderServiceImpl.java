package com.example.test_order_service.serviceImpl;

import com.example.test_order_service.dto.repsonse.PageResponse;
import com.example.test_order_service.dto.repsonse.RestResponse;
import com.example.test_order_service.dto.repsonse.TestOrderResponse;
import com.example.test_order_service.dto.request.TestOrderRequest;
import com.example.test_order_service.dto.request.TestOrderUpdateRequest;
import com.example.test_order_service.entity.TestOrder;
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
        int age = calculateAge(request.getDateOfBirth());

        TestOrderResponse response = testOrderMapper.toTestOrderResponse(testOrderRepository.save(testOrder));
        response.setAge(age);

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

    private int calculateAge(LocalDate dateOfBirth) {
        LocalDate currentDate = LocalDate.now();
        return currentDate.getYear() - dateOfBirth.getYear();
    }

    @Override
    public RestResponse<TestOrderResponse> updateTestOrder(String orderId, TestOrderUpdateRequest request) {
        try {
            TestOrder testOrder = testOrderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Test order not found"));

            testOrder.setPatientName(request.getPatientName() != null ? request.getPatientName() : testOrder.getPatientName());
            testOrder.setDateOfBirth(request.getDateOfBirth() != null ? request.getDateOfBirth() : testOrder.getDateOfBirth());
            testOrder.setGender(request.getGender() != null ? request.getGender() : testOrder.getGender());
            testOrder.setPhone(request.getPhone() != null ? request.getPhone() : testOrder.getPhone());
            testOrder.setAddress(request.getAddress() != null ? request.getAddress() : testOrder.getAddress());
            testOrder.setEmail(request.getEmail() != null ? request.getEmail() : testOrder.getEmail());

            int age = calculateAge(testOrder.getDateOfBirth());

            TestOrderResponse response = testOrderMapper.toTestOrderResponse(testOrderRepository.save(testOrder));
            response.setAge(age);

            return RestResponse.<TestOrderResponse>builder()
                    .statusCode(200)
                    .result(response)
                    .message("Test order updated successfully")
                    .timestamp(LocalDateTime.now())
                    .build();

        } catch (RuntimeException e) {
            return RestResponse.<TestOrderResponse>builder()
                    .statusCode(404)
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            return RestResponse.<TestOrderResponse>builder()
                    .statusCode(500)
                    .message("An error occurred while updating the test order")
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }
}
