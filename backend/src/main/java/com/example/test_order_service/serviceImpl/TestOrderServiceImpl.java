package com.example.test_order_service.serviceImpl;

import com.example.test_order_service.dto.response.*;
import com.example.test_order_service.dto.request.TestOrderRequest;
import com.example.test_order_service.dto.request.TestOrderUpdateRequest;
import com.example.test_order_service.entity.Comment;
import com.example.test_order_service.entity.TestOrder;
import com.example.test_order_service.entity.enumForEntity.TestOrderStatus;
import com.example.test_order_service.event.publisher.MonitoringEventPublisher;
import com.example.test_order_service.exception.ResourceNotFoundException;
import com.example.test_order_service.mapper.CommentMapper;
import com.example.test_order_service.mapper.TestOrderMapper;
import com.example.test_order_service.repository.TestOrderRepository;
import com.example.test_order_service.service.TestOrderService;
import com.example.test_order_service.utils.GeneralUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class TestOrderServiceImpl implements TestOrderService {

    private final TestOrderRepository testOrderRepository;
    private final TestOrderMapper testOrderMapper;
    private final CommentMapper commentMapper;
    private final InstrumentSyncService instrumentSyncService;
    
    // Optional: Event publisher sẽ chỉ inject nếu có sẵn
    @Autowired(required = false)
    private MonitoringEventPublisher eventPublisher;

    @Override
    public RestResponse<TestOrderResponse> createTestOrder(TestOrderRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("No test results provided");
        }

        TestOrder testOrder = testOrderMapper.toTestOrderEntity(request);
        testOrder.setCreatedBy("System");

        testOrder.setBloodCollectionId(GeneralUtils.generateBloodCollectionId(testOrderRepository.countByDateCode(
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        )));

        // Save trước - business logic quan trọng nhất!
        TestOrder savedOrder = testOrderRepository.save(testOrder);

        // Publish event (nếu event publisher có sẵn)
//        if (eventPublisher != null) {
//            eventPublisher.publishTestOrderCreated(savedOrder);
//        }

        TestOrderResponse response = testOrderMapper.toTestOrderResponse(savedOrder);

//        try {
//            instrumentSyncService.requestOrWait(savedOrder.getTestOrderId());
//        } catch (Exception ex) {
//        }

        return RestResponse.<TestOrderResponse>builder()
                .statusCode(200)
                .result(response)
                .message("Test order created successfully")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Override
//    @Transactional(readOnly = true)
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
        
        // Lưu status cũ trước khi update
        TestOrderStatus oldStatus = testOrder.getStatus();
        
        // Update các fields
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

        TestOrder savedOrder = testOrderRepository.save(testOrder);
        
        // Publish event nếu status thay đổi
        if (eventPublisher != null && 
            request.getStatus() != null && 
            !oldStatus.equals(savedOrder.getStatus())) {
            
            eventPublisher.publishStatusChanged(orderId, oldStatus, savedOrder.getStatus());
        }

        TestOrderResponse response = testOrderMapper.toTestOrderResponse(savedOrder);

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
//    @Transactional(readOnly = true)
    public RestResponse<TestOrderDetailResponse> getTestOrderById(String orderId) {
        TestOrder testOrder = testOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Test order not found"));

        TestOrderDetailResponse testOrderDetailResponse = testOrderMapper.toTestOrderDetailResponse(testOrder);
        testOrderDetailResponse.setAge(GeneralUtils.calculateAge(testOrder.getDateOfBirth()));

        List<CommentResponse> sortedComments = testOrder.getComments().stream()
                .sorted(Comparator.comparing(Comment::getCreatedAt))
                .map(commentMapper::toCommentResponse)
                .toList();

        testOrderDetailResponse.setComments(sortedComments);

        return RestResponse.<TestOrderDetailResponse>builder()
                .statusCode(200)
                .result(testOrderDetailResponse)
                .message("Test order retrieved successfully")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Override
    public RestResponse<TestOrderStatisticResponse> getTestOrderStatistics() {
        long total = testOrderRepository.countActive();
        var groupedCounts = testOrderRepository.countByStatus();

        long pending = 0;
        long completed = 0;
        long cancelled = 0;
        long reviewed = 0;
        long aiReviewed = 0;

        for (Object[] row : groupedCounts) {
            String status = row[0].toString();
            long count = (long) row[1];
            switch (status) {
                case "PENDING" -> pending = count;
                case "COMPLETED" -> completed = count;
                case "CANCELLED" -> cancelled = count;
                case "REVIEWED" -> reviewed = count;
                case "AI_REVIEWED" -> aiReviewed = count;
            }
        }

        TestOrderStatisticResponse statistic = TestOrderStatisticResponse.builder()
                .total(total)
                .pending(pending)
                .completed(completed)
                .cancelled(cancelled)
                .reviewed(reviewed)
                .aiReviewed(aiReviewed)
                .build();

        return RestResponse.<TestOrderStatisticResponse>builder()
                .statusCode(200)
                .message("Statistics retrieved successfully")
                .result(statistic)
                .timestamp(java.time.LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional
    public RestResponse<TestOrderResponse> reviewTestOrder(String orderId) {
        TestOrder testOrder = testOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Test order not found"));

        // Validate current status is COMPLETED
        if (testOrder.getStatus() != TestOrderStatus.COMPLETED) {
            throw new IllegalStateException("Only COMPLETED test orders can be reviewed. Current status: " + testOrder.getStatus());
        }

        // Update status to REVIEWED
        TestOrderStatus oldStatus = testOrder.getStatus();
        testOrder.setStatus(TestOrderStatus.REVIEWED);
        testOrder.setReviewedAt(LocalDateTime.now());
        testOrder.setReviewedBy("System"); // TODO: Replace with actual user from security context

        TestOrder savedOrder = testOrderRepository.save(testOrder);

        // Publish event if event publisher is available
        if (eventPublisher != null) {
            eventPublisher.publishStatusChanged(orderId, oldStatus, TestOrderStatus.REVIEWED);
        }

        TestOrderResponse response = testOrderMapper.toTestOrderResponse(savedOrder);

        return RestResponse.<TestOrderResponse>builder()
                .statusCode(200)
                .result(response)
                .message("Test order reviewed successfully")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Override
    public PageResponse<TestOrderDetailResponse> getTestOrderByEmail(Pageable pageable, String email) {
        Page<TestOrder> testOrderPage = testOrderRepository.findByEmail(pageable, email);

        return PageResponse.<TestOrderDetailResponse>builder()
                .currentPage(testOrderPage.getNumber() + 1)
                .totalPages(testOrderPage.getTotalPages())
                .items(testOrderPage.stream()
                        .map(testOrderMapper::toTestOrderDetailResponse)
                        .toList())
                .build();
    }

    @Override
    public RestResponse<DailyStatisticsResponse> getDailyStatistics() {
        // Xác định tuần hiện tại (Thứ 2 -> Chủ nhật)
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = startOfWeek.plusDays(7); // Chủ nhật + 1 ngày

        LocalDateTime startDateTime = startOfWeek.atStartOfDay();
        LocalDateTime endDateTime = endOfWeek.atStartOfDay();

        // Lấy tất cả test orders trong tuần
        List<TestOrder> weeklyOrders = testOrderRepository.findTestOrdersInCurrentWeek(startDateTime, endDateTime);

        // Khởi tạo map để nhóm theo ngày
        Map<DayOfWeek, DailyStatisticsResponse.DailyData> dailyMap = new HashMap<>();

        // Khởi tạo tất cả các ngày trong tuần với giá trị 0
        String[] dayNames = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (int i = 0; i < 7; i++) {
            DayOfWeek dayOfWeek = DayOfWeek.of(i + 1); // MONDAY = 1, SUNDAY = 7
            dailyMap.put(dayOfWeek, DailyStatisticsResponse.DailyData.builder()
                    .day(dayNames[i])
                    .pending(0)
                    .completed(0)
                    .reviewed(0)
                    .build());
        }

        // Xử lý từng test order
        for (TestOrder order : weeklyOrders) {
            // 1. Pending: Đếm orders được tạo trong ngày và vẫn pending (status hiện tại = PENDING)
            if (order.getCreatedAt() != null && order.getStatus() == TestOrderStatus.PENDING) {
                DayOfWeek createdDay = order.getCreatedAt().getDayOfWeek();
                DailyStatisticsResponse.DailyData dailyData = dailyMap.get(createdDay);
                dailyData.setPending(dailyData.getPending() + 1);
            }

            // 2. Completed: Đếm orders có test result trong ngày (runAt trong tuần này)
            // runAt là lúc test được chạy và có kết quả
            if (order.getRunAt() != null) {
                LocalDateTime runAtTime = order.getRunAt();
                // Check if runAt is within current week
                if (!runAtTime.isBefore(startDateTime) && runAtTime.isBefore(endDateTime)) {
                    DayOfWeek completedDay = runAtTime.getDayOfWeek();
                    DailyStatisticsResponse.DailyData dailyData = dailyMap.get(completedDay);
                    dailyData.setCompleted(dailyData.getCompleted() + 1);
                }
            }

            // 3. Reviewed: Đếm orders được review trong ngày (reviewedAt trong tuần này)
            if (order.getReviewedAt() != null) {
                LocalDateTime reviewedAtTime = order.getReviewedAt();
                // Check if reviewedAt is within current week
                if (!reviewedAtTime.isBefore(startDateTime) && reviewedAtTime.isBefore(endDateTime)) {
                    DayOfWeek reviewedDay = reviewedAtTime.getDayOfWeek();
                    DailyStatisticsResponse.DailyData dailyData = dailyMap.get(reviewedDay);
                    dailyData.setReviewed(dailyData.getReviewed() + 1);
                }
            }
        }

        // Chuyển map thành list theo thứ tự từ Monday -> Sunday
        List<DailyStatisticsResponse.DailyData> dailyDataList = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            dailyDataList.add(dailyMap.get(DayOfWeek.of(i)));
        }

        DailyStatisticsResponse response = DailyStatisticsResponse.builder()
                .dailyData(dailyDataList)
                .build();

        return RestResponse.<DailyStatisticsResponse>builder()
                .statusCode(200)
                .message("Daily statistics retrieved successfully")
                .result(response)
                .timestamp(LocalDateTime.now())
                .build();
    }
}