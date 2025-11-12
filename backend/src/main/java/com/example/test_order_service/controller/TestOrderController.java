package com.example.test_order_service.controller;

import com.example.test_order_service.dto.response.PageResponse;
import com.example.test_order_service.dto.response.RestResponse;
import com.example.test_order_service.dto.response.TestOrderDetailResponse;
import com.example.test_order_service.dto.response.TestOrderResponse;
import com.example.test_order_service.dto.request.TestOrderRequest;
import com.example.test_order_service.dto.request.TestOrderUpdateRequest;
import com.example.test_order_service.entity.TestOrder;
import com.example.test_order_service.ingest.publisher.ResyncRequestPublisher;
import com.example.test_order_service.repository.TestOrderRepository;
import com.example.test_order_service.service.TestOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.ResourceNotFoundException;
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
    private final ResyncRequestPublisher resyncRequestPublisher;
    private final TestOrderRepository testOrderRepository;

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

    @GetMapping("/{orderId}")
    public RestResponse<TestOrderDetailResponse> getTestOrderById(@PathVariable String orderId) {
        return testOrderService.getTestOrderById(orderId);
    }

    @GetMapping
    public RestResponse<PageResponse<TestOrderResponse>> getTestOrders(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(defaultValue = "patientName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
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
    public RestResponse<PageResponse<TestOrderResponse>> deleteTestOrder(
            @PathVariable String orderId,
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(defaultValue = "patientName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;

        int pageIndex = page < 1 ? 0 : page - 1;

        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by(direction, sortBy));

        PageResponse<TestOrderResponse> testOrderPage = testOrderService.deleteTestOrder(orderId, pageable, keyword);

        return RestResponse.<PageResponse<TestOrderResponse>>builder()
                .timestamp(LocalDateTime.now())
                .statusCode(200)
                .message("Test orders retrieved successfully")
                .result(testOrderPage)
                .build();
    }

    @GetMapping("/statistics")
    public RestResponse<?> getTestOrderStatistics() {
        return testOrderService.getTestOrderStatistics();
    }

    @PatchMapping("/{orderId}/review")
    public RestResponse<TestOrderResponse> reviewTestOrder(@PathVariable String orderId) {
        return testOrderService.reviewTestOrder(orderId);
    }
    //Gửi yêu cầu đồng bộ kết quả xét nghiệm cho một đơn hàng cụ thể
    @PostMapping("/{orderId}/resync")
    public RestResponse<Void> resyncTestOrderResults(@PathVariable String orderId) {

        TestOrder order = testOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("TestOrder not found with id: " + orderId));

        String bloodCollectionId = order.getBloodCollectionId();

        resyncRequestPublisher.requestResync(orderId, bloodCollectionId, "ManualTriggerByUser");

        return RestResponse.<Void>builder()
                .statusCode(202) // 202 Accepted
                .message("Resync request for orderId '" + orderId + "' and bloodCollectionId '" + bloodCollectionId + "' has been sent successfully.")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @GetMapping("/email/")
    public RestResponse<PageResponse<TestOrderDetailResponse>> getTestOrderByEmail(
            @RequestParam(required = false, defaultValue = "") String email,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(defaultValue = "createdBy") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;

        int pageIndex = page < 1 ? 0 : page - 1;

        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by(direction, sortBy));

        PageResponse<TestOrderDetailResponse> testOrderPage = testOrderService.getTestOrderByEmail(pageable, email);

        return RestResponse.<PageResponse<TestOrderDetailResponse>>builder()
                .timestamp(LocalDateTime.now())
                .statusCode(200)
                .message("Test orders retrieved successfully")
                .result(testOrderPage)
                .build();
    }
}
