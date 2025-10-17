package com.example.test_order_service.controller;

import com.example.test_order_service.dto.repsonse.RestResponse;
import com.example.test_order_service.dto.repsonse.TestOrderResponse;
import com.example.test_order_service.dto.request.TestOrderRequest;
import com.example.test_order_service.dto.request.TestOrderUpdateRequest;
import com.example.test_order_service.service.TestOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test-orders")
@RequiredArgsConstructor
public class TestOrderController {
    private final TestOrderService testOrderService;

    @PostMapping
    public RestResponse<TestOrderResponse> createTestOrder(@RequestBody @Valid TestOrderRequest request) {
        return testOrderService.createTestOrder(request);
    }

    @PutMapping("/{orderId}")
    public RestResponse<TestOrderResponse> updateTestOrder(
            @PathVariable String orderId,
            @RequestBody @Valid TestOrderUpdateRequest request) {
        return testOrderService.updateTestOrder(orderId, request);
    }
}
