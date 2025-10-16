package com.example.test_order_service.serviceImpl;

import com.example.test_order_service.dto.repsonse.RestResponse;
import com.example.test_order_service.dto.repsonse.TestOrderResponse;
import com.example.test_order_service.dto.request.TestOrderRequest;
import com.example.test_order_service.entity.TestOrder;
import com.example.test_order_service.mapper.TestOrderMapper;
import com.example.test_order_service.repository.TestOrderRepository;
import com.example.test_order_service.service.TestOrderService;
import lombok.RequiredArgsConstructor;
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

    private int calculateAge(LocalDate dateOfBirth) {
        LocalDate currentDate = LocalDate.now();
        return currentDate.getYear() - dateOfBirth.getYear();
    }
}
