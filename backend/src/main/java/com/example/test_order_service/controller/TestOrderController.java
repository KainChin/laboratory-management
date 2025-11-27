package com.example.test_order_service.controller;

import com.example.test_order_service.dto.response.PageResponse;
import com.example.test_order_service.dto.response.PatientListResponse;
import com.example.test_order_service.dto.response.RestResponse;
import com.example.test_order_service.dto.response.TestOrderDetailResponse;
import com.example.test_order_service.dto.response.TestOrderResponse;
import com.example.test_order_service.dto.request.TestOrderRequest;
import com.example.test_order_service.dto.request.TestOrderUpdateRequest;
import com.example.test_order_service.entity.enumForEntity.TestOrderStatus;
import com.example.test_order_service.ingest.publisher.ResyncRequestPublisher;
import com.example.test_order_service.integration.patient.PatientServiceClient;
import com.example.test_order_service.repository.TestOrderRepository;
import com.example.test_order_service.service.TestOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/test-orders")
@RequiredArgsConstructor
public class TestOrderController {
    private final TestOrderService testOrderService;
    private final ResyncRequestPublisher resyncRequestPublisher;
    private final TestOrderRepository testOrderRepository;
    private final PatientServiceClient patientServiceClient;

    @PostMapping
    @PreAuthorize("hasAnyRole('LAB_USER', 'ADMIN') and hasAuthority('CREATE_TEST_ORDER')")
    public RestResponse<TestOrderResponse> createTestOrder(
            @RequestBody @Valid TestOrderRequest request,
            @RequestHeader(value = "Authorization", required = true) String authToken) {
        return testOrderService.createTestOrder(request, authToken);
    }

    @GetMapping("/patients")
    @PreAuthorize("hasAnyRole('LAB_USER', 'ADMIN') and hasAuthority('CREATE_TEST_ORDER')")
    public RestResponse<PatientListResponse> getPatients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "patientId,desc") String sort,
            @RequestHeader(value = "Authorization", required = true) String authToken) {

        PatientListResponse patients = patientServiceClient.getAllPatients(page, pageSize, keyword, sort, authToken);

        return RestResponse.<PatientListResponse>builder()
                .statusCode(200)
                .message("Patients retrieved successfully")
                .result(patients)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @PutMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('LAB_USER', 'ADMIN') and hasAuthority('MODIFY_TEST_ORDER')")
    public RestResponse<TestOrderResponse> updateTestOrder(
            @PathVariable String orderId,
            @RequestBody @Valid TestOrderUpdateRequest request) {
        return testOrderService.updateTestOrder(orderId, request);
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('LAB_USER', 'ADMIN') and hasAuthority('REVIEW_TEST_ORDER')")
    public RestResponse<TestOrderDetailResponse> getTestOrderById(@PathVariable String orderId) {
        return testOrderService.getTestOrderById(orderId);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('LAB_USER', 'ADMIN') and hasAuthority('REVIEW_TEST_ORDER')")
    public RestResponse<PageResponse<TestOrderResponse>> getTestOrders(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) TestOrderStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(defaultValue = "patientName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        int pageIndex = page < 1 ? 0 : page - 1;
        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by(direction, sortBy));

        PageResponse<TestOrderResponse> testOrderPage = testOrderService.getTestOrders(pageable, keyword, startDate, endDate, status);

        return RestResponse.<PageResponse<TestOrderResponse>>builder()
                .timestamp(LocalDateTime.now())
                .statusCode(200)
                .message("Test orders retrieved successfully")
                .result(testOrderPage)
                .build();
    }

    @DeleteMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('LAB_USER', 'ADMIN') and hasAuthority('DELETE_TEST_ORDER')")
    public RestResponse<PageResponse<TestOrderResponse>> deleteTestOrder(
            @PathVariable String orderId,
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) TestOrderStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(defaultValue = "patientName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;

        int pageIndex = page < 1 ? 0 : page - 1;

        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by(direction, sortBy));

        PageResponse<TestOrderResponse> testOrderPage = testOrderService.deleteTestOrder(
                orderId, pageable, keyword, startDate, endDate, status
        );


        return RestResponse.<PageResponse<TestOrderResponse>>builder()
                .timestamp(LocalDateTime.now())
                .statusCode(200)
                .message("Test orders retrieved successfully")
                .result(testOrderPage)
                .build();
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('LAB_USER', 'ADMIN') and hasAuthority('REVIEW_TEST_ORDER')")
    public RestResponse<?> getTestOrderStatistics() {
        return testOrderService.getTestOrderStatistics();
    }

    @GetMapping("/daily-statistics")
    @PreAuthorize("hasAnyRole('LAB_USER', 'ADMIN') and hasAuthority('REVIEW_TEST_ORDER')")
    public RestResponse<?> getDailyStatistics() {
        return testOrderService.getDailyStatistics();
    }

    @PatchMapping("/{orderId}/review")
    @PreAuthorize("hasAnyRole('LAB_USER', 'ADMIN') and hasAuthority('REVIEW_TEST_ORDER')")
    public RestResponse<TestOrderResponse> reviewTestOrder(@PathVariable String orderId) {
        return testOrderService.reviewTestOrder(orderId);
    }

    //Gửi yêu cầu đồng bộ kết quả xét nghiệm cho một đơn hàng cụ thể
    @PostMapping("/{orderId}/resync")
    @PreAuthorize("hasAnyRole('LAB_USER', 'ADMIN')")
    public RestResponse<Void> resyncTestOrderResults(@PathVariable String orderId) {
        return testOrderService.resyncTestOrderResults(orderId);
    }

    @GetMapping("/email")
    @PreAuthorize("hasAnyRole('LAB_USER', 'ADMIN') and hasAuthority('REVIEW_TEST_ORDER')")
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

    @GetMapping("/blood-collection/{bloodCollectionId}")
    public RestResponse<TestOrderResponse> getTestOrderByBloodCollectionId(@PathVariable String bloodCollectionId) {
        return testOrderService.getTestOrderByBloodCollectionId(bloodCollectionId);
    }
}
