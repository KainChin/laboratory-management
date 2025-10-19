package com.example.test_order_service.controller;

import com.example.test_order_service.dto.repsonse.PageResponse;
import com.example.test_order_service.dto.repsonse.RestResponse;
import com.example.test_order_service.dto.repsonse.TestOrderResponse;
import com.example.test_order_service.dto.request.TestOrderRequest;
import com.example.test_order_service.dto.request.TestOrderUpdateRequest;
import com.example.test_order_service.service.TestOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;

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

    @GetMapping
    public RestResponse<PageResponse<TestOrderResponse>> getTestOrders(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(defaultValue = "patientName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;

        int pageIndex = page < 1 ? 0 : page - 1;

        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by(direction, sortBy));

        PageResponse<TestOrderResponse> testOrderPage = testOrderService.getTestOrders(pageable, keyword);

        return RestResponse.<PageResponse<TestOrderResponse>>builder()
                .timestamp(LocalDateTime.now())
                .statusCode(200)
                .message("Test orders retrieved successfully")
                .result(testOrderPage)
                .build();
    }

    @DeleteMapping("/{orderId}")
    public RestResponse<Void> deleteTestOrder(@PathVariable String orderId) {
        return testOrderService.deleteTestOrder(orderId);
    }
}
