package com.example.test_order_service.serviceImpl;

import com.example.test_order_service.dto.request.TestOrderRequest;
import com.example.test_order_service.dto.request.TestOrderUpdateRequest;
import com.example.test_order_service.dto.response.*;
import com.example.test_order_service.entity.Comment;
import com.example.test_order_service.entity.TestOrder;
import com.example.test_order_service.entity.enumForEntity.TestOrderStatus;
import com.example.test_order_service.event.publisher.MonitoringEventPublisher;
import com.example.test_order_service.exception.ResourceNotFoundException;
import com.example.test_order_service.integration.patient.PatientServiceClient;
import com.example.test_order_service.mapper.CommentMapper;
import com.example.test_order_service.mapper.TestOrderMapper;
import com.example.test_order_service.repository.TestOrderRepository;
import com.example.test_order_service.utils.GeneralUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TestOrderServiceImplTest {

    @Mock TestOrderRepository repository;
    @Mock TestOrderMapper mapper;
    @Mock CommentMapper commentMapper;
    @Mock InstrumentSyncService instrumentSyncService;
    @Mock PatientServiceClient patientServiceClient;

    @InjectMocks TestOrderServiceImpl service;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    private TestOrder baseOrder(String id, boolean deleted, TestOrderStatus status) {
        TestOrder o = new TestOrder();
        o.setTestOrderId(id);
        o.setPatientId(1);
        o.setPatientName("A");
        o.setCitizenId("123");
        o.setCountry("VN");
        o.setBloodCollectionId("BC-" + id);
        o.setDeleted(deleted);
        o.setStatus(status);
        o.setCreatedAt(LocalDateTime.now().minusDays(1));
        return o;
    }

    // -------- createTestOrder --------

    @Test
    void createTestOrder_nullRequest_shouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> service.createTestOrder(null, "token"));
    }

    @Test
    void createTestOrder_success_shouldGenerateBloodCollectionIdAndSave() {
        TestOrderRequest req = TestOrderRequest.builder()
                .patientId(1)
                .patientName("A")
                .identityNumber("123")
                .country("VN")
                .phone("0123")
                .build();

        // ✅ PatientDto mock luôn trả về "active" cho mọi kiểu check
        PatientDto patientMock = mock(PatientDto.class, invocation -> {
            String m = invocation.getMethod().getName().toLowerCase();
            Class<?> rt = invocation.getMethod().getReturnType();

            if (rt == boolean.class || rt == Boolean.class) return true;
            if (rt == String.class && (m.contains("status") || m.contains("active")))
                return "ACTIVE";

            return Answers.RETURNS_DEFAULTS.answer(invocation);
        });

        when(patientServiceClient.getPatientById(eq(1), anyString()))
                .thenReturn(patientMock);

        TestOrder entity = baseOrder(null, false, TestOrderStatus.PENDING);
        TestOrder saved = baseOrder("to1", false, TestOrderStatus.PENDING);
        TestOrderResponse dto = TestOrderResponse.builder()
                .testOrderId("to1")
                .bloodCollectionId("BCODE")
                .build();

        when(mapper.toTestOrderEntity(req)).thenReturn(entity);
        when(repository.countByDateCode(anyString())).thenReturn(5L);
        when(repository.save(any(TestOrder.class))).thenReturn(saved);
        when(mapper.toTestOrderResponse(saved)).thenReturn(dto);

        try (MockedStatic<GeneralUtils> mocked = mockStatic(GeneralUtils.class)) {
            mocked.when(GeneralUtils::getCurrentUsername).thenReturn("user1");
            mocked.when(() -> GeneralUtils.generateBloodCollectionId(5L)).thenReturn("BCODE");

            RestResponse<TestOrderResponse> res = service.createTestOrder(req, "token");

            assertEquals(200, res.getStatusCode());
            assertEquals("Test order created successfully", res.getMessage());
            assertEquals("to1", res.getResult().getTestOrderId());
            assertEquals("BCODE", entity.getBloodCollectionId());
        }
    }

    // -------- getTestOrders --------

    @Test
    void getTestOrders_shouldConvertDatesCallRepoAndMap() {
        Pageable pageable = PageRequest.of(0, 2);
        TestOrder o1 = baseOrder("to1", false, TestOrderStatus.PENDING);
        Page<TestOrder> page = new PageImpl<>(List.of(o1), pageable, 1);

        when(repository.findTestOrdersByParams(eq(pageable), eq("k"), any(), any(), eq(null)))
                .thenReturn(page);
        when(mapper.toTestOrderResponse(o1))
                .thenReturn(TestOrderResponse.builder().testOrderId("to1").build());

        PageResponse<TestOrderResponse> res =
                service.getTestOrders(pageable, "k",
                        LocalDate.of(2025, 1, 1),
                        LocalDate.of(2025, 1, 2),
                        null);

        assertEquals(1, res.getCurrentPage());
        assertEquals(1, res.getTotalPages());
        assertEquals(1, res.getItems().size());
        verify(repository).findTestOrdersByParams(eq(pageable), eq("k"),
                any(LocalDateTime.class), any(LocalDateTime.class), isNull());
    }

    // -------- updateTestOrder --------

    @Test
    void updateTestOrder_notFound_shouldThrow() {
        when(repository.findById("to1")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> service.updateTestOrder("to1", new TestOrderUpdateRequest()));
    }

    @Test
    void updateTestOrder_deleted_shouldThrow() {
        when(repository.findById("to1"))
                .thenReturn(Optional.of(baseOrder("to1", true, TestOrderStatus.PENDING)));
        assertThrows(ResourceNotFoundException.class,
                () -> service.updateTestOrder("to1", new TestOrderUpdateRequest()));
    }

    @Test
    void updateTestOrder_statusChanged_shouldPublishEventWhenPublisherPresent() throws Exception {
        TestOrder order = baseOrder("to1", false, TestOrderStatus.PENDING);
        when(repository.findById("to1")).thenReturn(Optional.of(order));
        when(repository.save(any(TestOrder.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toTestOrderResponse(any()))
                .thenReturn(TestOrderResponse.builder().testOrderId("to1").build());

        MonitoringEventPublisher publisher = mock(MonitoringEventPublisher.class);
        Field f = TestOrderServiceImpl.class.getDeclaredField("eventPublisher");
        f.setAccessible(true);
        f.set(service, publisher);

        TestOrderUpdateRequest req = TestOrderUpdateRequest.builder()
                .status(TestOrderStatus.COMPLETED)
                .patientName("B")
                .build();

        try (MockedStatic<GeneralUtils> mocked = mockStatic(GeneralUtils.class)) {
            mocked.when(GeneralUtils::getCurrentUsername).thenReturn("user1");

            RestResponse<TestOrderResponse> res = service.updateTestOrder("to1", req);

            assertEquals(200, res.getStatusCode());
            verify(publisher)
                    .publishStatusChanged("to1", TestOrderStatus.PENDING, TestOrderStatus.COMPLETED);
        }
    }

    // -------- deleteTestOrder --------

    @Test
    void deleteTestOrder_success_shouldMarkDeletedAndReturnList() {
        TestOrder order = baseOrder("to1", false, TestOrderStatus.PENDING);
        when(repository.findById("to1")).thenReturn(Optional.of(order));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Pageable pageable = PageRequest.of(0, 10);
        when(repository.findTestOrdersByParams(eq(pageable), anyString(), any(), any(), any()))
                .thenReturn(Page.empty(pageable));

        try (MockedStatic<GeneralUtils> mocked = mockStatic(GeneralUtils.class)) {
            mocked.when(GeneralUtils::getCurrentUsername).thenReturn("user1");

            PageResponse<TestOrderResponse> res =
                    service.deleteTestOrder("to1", pageable, "", null, null, null);

            assertTrue(order.isDeleted());
            assertEquals(0, res.getItems().size());
        }
    }

    // -------- getTestOrderById --------

    @Test
    void getTestOrderById_success_shouldSetAgeAndSortedComments() {
        TestOrder order = baseOrder("to1", false, TestOrderStatus.PENDING);

        Comment c1 = new Comment();
        c1.setCreatedAt(LocalDateTime.now().minusHours(2));
        Comment c2 = new Comment();
        c2.setCreatedAt(LocalDateTime.now().minusHours(1));
        order.setComments(List.of(c2, c1));

        when(repository.findById("to1")).thenReturn(Optional.of(order));
        when(mapper.toTestOrderDetailResponse(order))
                .thenReturn(TestOrderDetailResponse.builder().testOrderId("to1").build());

        when(commentMapper.toCommentResponse(c1))
                .thenReturn(CommentResponse.builder().commentId("c1").build());
        when(commentMapper.toCommentResponse(c2))
                .thenReturn(CommentResponse.builder().commentId("c2").build());

        try (MockedStatic<GeneralUtils> mocked = mockStatic(GeneralUtils.class)) {
            mocked.when(() -> GeneralUtils.calculateAge(any())).thenReturn(25);

            RestResponse<TestOrderDetailResponse> res = service.getTestOrderById("to1");

            assertEquals(25, res.getResult().getAge());
            assertEquals(List.of("c1", "c2"),
                    res.getResult().getComments().stream()
                            .map(CommentResponse::getCommentId).toList());
        }
    }

    // -------- getTestOrderStatistics --------

    @Test
    void getTestOrderStatistics_shouldAggregateCounts() {
        when(repository.countActive()).thenReturn(10L);
        when(repository.countByStatus()).thenReturn(List.of(
                new Object[]{TestOrderStatus.PENDING, 2L},
                new Object[]{TestOrderStatus.COMPLETED, 3L},
                new Object[]{TestOrderStatus.REVIEWED, 1L}
        ));

        RestResponse<TestOrderStatisticResponse> res = service.getTestOrderStatistics();

        assertEquals(10, res.getResult().getTotal());
        assertEquals(2, res.getResult().getPending());
        assertEquals(3, res.getResult().getCompleted());
        assertEquals(1, res.getResult().getReviewed());
    }

    // -------- reviewTestOrder --------

    @Test
    void reviewTestOrder_wrongStatus_shouldThrow() {
        TestOrder order = baseOrder("to1", false, TestOrderStatus.PENDING);
        when(repository.findById("to1")).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class,
                () -> service.reviewTestOrder("to1"));
    }

    @Test
    void reviewTestOrder_success_shouldSetReviewedAndPublishWhenPublisherPresent() throws Exception {
        TestOrder order = baseOrder("to1", false, TestOrderStatus.COMPLETED);
        when(repository.findById("to1")).thenReturn(Optional.of(order));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toTestOrderResponse(any()))
                .thenReturn(TestOrderResponse.builder().testOrderId("to1").build());

        MonitoringEventPublisher publisher = mock(MonitoringEventPublisher.class);
        Field f = TestOrderServiceImpl.class.getDeclaredField("eventPublisher");
        f.setAccessible(true);
        f.set(service, publisher);

        try (MockedStatic<GeneralUtils> mocked = mockStatic(GeneralUtils.class)) {
            mocked.when(GeneralUtils::getCurrentUsername).thenReturn("user1");

            RestResponse<TestOrderResponse> res = service.reviewTestOrder("to1");
            assertEquals(TestOrderStatus.REVIEWED, order.getStatus());
            verify(publisher).publishStatusChanged("to1",
                    TestOrderStatus.COMPLETED, TestOrderStatus.REVIEWED);
        }
    }

    // -------- getTestOrderByEmail --------

    @Test
    void getTestOrderByEmail_shouldReturnPageResponse() {
        Pageable pageable = PageRequest.of(0, 1);
        TestOrder order = baseOrder("to1", false, TestOrderStatus.PENDING);
        Page<TestOrder> page = new PageImpl<>(List.of(order), pageable, 1);

        when(repository.findByEmail(pageable, "a@b.com")).thenReturn(page);
        when(mapper.toTestOrderDetailResponse(order))
                .thenReturn(TestOrderDetailResponse.builder().testOrderId("to1").build());

        PageResponse<TestOrderDetailResponse> res =
                service.getTestOrderByEmail(pageable, "a@b.com");

        assertEquals(1, res.getItems().size());
        assertEquals("to1", res.getItems().get(0).getTestOrderId());
    }

    // -------- getDailyStatistics --------

    @Test
    void getDailyStatistics_shouldCountPendingCompletedReviewedPerDay() {
        TestOrder pendingMon = baseOrder("p1", false, TestOrderStatus.PENDING);
        pendingMon.setCreatedAt(LocalDateTime.now().with(java.time.DayOfWeek.MONDAY));

        TestOrder completedTue = baseOrder("c1", false, TestOrderStatus.COMPLETED);
        completedTue.setRunAt(LocalDateTime.now().with(java.time.DayOfWeek.TUESDAY));

        TestOrder reviewedWed = baseOrder("r1", false, TestOrderStatus.REVIEWED);
        reviewedWed.setReviewedAt(LocalDateTime.now().with(java.time.DayOfWeek.WEDNESDAY));

        when(repository.findTestOrdersInCurrentWeek(any(), any()))
                .thenReturn(List.of(pendingMon, completedTue, reviewedWed));

        RestResponse<DailyStatisticsResponse> res = service.getDailyStatistics();

        List<DailyStatisticsResponse.DailyData> list = res.getResult().getDailyData();
        assertEquals(7, list.size());
        assertEquals(1, list.get(0).getPending());
        assertEquals(1, list.get(1).getCompleted());
        assertEquals(1, list.get(2).getReviewed());
    }

    // -------- getTestOrderByBloodCollectionId --------

    @Test
    void getTestOrderByBloodCollectionId_notFound_shouldThrow() {
        when(repository.findByBloodCollectionId("bc1")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> service.getTestOrderByBloodCollectionId("bc1"));
    }

    @Test
    void getTestOrderByBloodCollectionId_success() {
        TestOrder order = baseOrder("to1", false, TestOrderStatus.PENDING);
        when(repository.findByBloodCollectionId("bc1")).thenReturn(Optional.of(order));
        when(mapper.toTestOrderResponse(order))
                .thenReturn(TestOrderResponse.builder().testOrderId("to1").build());

        RestResponse<TestOrderResponse> res =
                service.getTestOrderByBloodCollectionId("bc1");

        assertEquals(200, res.getStatusCode());
        assertEquals("to1", res.getResult().getTestOrderId());
    }
}
