package com.example.test_order_service.controller;

import com.example.test_order_service.dto.request.TestOrderRequest;
import com.example.test_order_service.dto.request.TestOrderUpdateRequest;
import com.example.test_order_service.dto.response.PageResponse;
import com.example.test_order_service.dto.response.RestResponse;
import com.example.test_order_service.dto.response.TestOrderDetailResponse;
import com.example.test_order_service.dto.response.TestOrderResponse;
import com.example.test_order_service.entity.TestOrder;
import com.example.test_order_service.entity.enumForEntity.TestOrderStatus;
import com.example.test_order_service.ingest.publisher.ResyncRequestPublisher;
import com.example.test_order_service.repository.TestOrderRepository;
import com.example.test_order_service.service.TestOrderService;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestOrderControllerTest {

    @Mock private TestOrderService testOrderService;
    @Mock private ResyncRequestPublisher resyncRequestPublisher;
    @Mock private TestOrderRepository testOrderRepository;

    @InjectMocks private TestOrderController controller;

    @Test
    void createTestOrder_shouldDelegateToService() {
        TestOrderRequest req = mock(TestOrderRequest.class);

        RestResponse<TestOrderResponse> serviceResp =
                RestResponse.<TestOrderResponse>builder()
                        .statusCode(200)
                        .message("ok")
                        .timestamp(LocalDateTime.now())
                        .result(mock(TestOrderResponse.class))
                        .build();

        when(testOrderService.createTestOrder(req)).thenReturn(serviceResp);

        RestResponse<TestOrderResponse> resp = controller.createTestOrder(req);

        assertSame(serviceResp, resp);
        verify(testOrderService).createTestOrder(req);
        verifyNoMoreInteractions(testOrderService, resyncRequestPublisher, testOrderRepository);
    }

    @Test
    void updateTestOrder_shouldDelegateToService() {
        TestOrderUpdateRequest req = mock(TestOrderUpdateRequest.class);

        RestResponse<TestOrderResponse> serviceResp =
                RestResponse.<TestOrderResponse>builder()
                        .statusCode(200)
                        .message("updated")
                        .timestamp(LocalDateTime.now())
                        .result(mock(TestOrderResponse.class))
                        .build();

        when(testOrderService.updateTestOrder("o1", req)).thenReturn(serviceResp);

        RestResponse<TestOrderResponse> resp = controller.updateTestOrder("o1", req);

        assertSame(serviceResp, resp);
        verify(testOrderService).updateTestOrder("o1", req);
        verifyNoMoreInteractions(testOrderService, resyncRequestPublisher, testOrderRepository);
    }

    @Test
    void getTestOrderById_shouldDelegateToService() {
        RestResponse<TestOrderDetailResponse> serviceResp =
                RestResponse.<TestOrderDetailResponse>builder()
                        .statusCode(200)
                        .message("found")
                        .timestamp(LocalDateTime.now())
                        .result(mock(TestOrderDetailResponse.class))
                        .build();

        when(testOrderService.getTestOrderById("o1")).thenReturn(serviceResp);

        RestResponse<TestOrderDetailResponse> resp = controller.getTestOrderById("o1");

        assertSame(serviceResp, resp);
        verify(testOrderService).getTestOrderById("o1");
        verifyNoMoreInteractions(testOrderService, resyncRequestPublisher, testOrderRepository);
    }

    @Test
    void getTestOrders_shouldBuildPageable_andWrapResponse_descBranch() {
        PageResponse<TestOrderResponse> pageResponse = mock(PageResponse.class);

        when(testOrderService.getTestOrders(any(Pageable.class), anyString(), any(), any(), any()))
                .thenReturn(pageResponse);

        RestResponse<PageResponse<TestOrderResponse>> resp =
                controller.getTestOrders(
                        "abc",
                        LocalDate.parse("2025-01-01"),
                        LocalDate.parse("2025-01-31"),
                        TestOrderStatus.PENDING,
                        2, 5,
                        "patientName", "desc"
                );

        assertEquals(200, resp.getStatusCode());
        assertEquals("Test orders retrieved successfully", resp.getMessage());
        assertSame(pageResponse, resp.getResult());
        assertNotNull(resp.getTimestamp());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(testOrderService).getTestOrders(
                pageableCaptor.capture(),
                eq("abc"),
                eq(LocalDate.parse("2025-01-01")),
                eq(LocalDate.parse("2025-01-31")),
                eq(TestOrderStatus.PENDING)
        );

        Pageable p = pageableCaptor.getValue();
        assertEquals(1, p.getPageNumber()); // page=2 -> index=1
        assertEquals(5, p.getPageSize());
        Sort.Order order = p.getSort().getOrderFor("patientName");
        assertNotNull(order);
        assertEquals(Sort.Direction.DESC, order.getDirection());

        verifyNoMoreInteractions(testOrderService, resyncRequestPublisher, testOrderRepository);
    }

    @Test
    void getTestOrders_whenPageLessThan1_andAscBranch() {
        PageResponse<TestOrderResponse> pageResponse = mock(PageResponse.class);

        when(testOrderService.getTestOrders(any(Pageable.class), anyString(), any(), any(), any()))
                .thenReturn(pageResponse);

        controller.getTestOrders(
                "",
                null, null, null,
                0, 6,
                "patientName", "asc"
        );

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(testOrderService).getTestOrders(pageableCaptor.capture(), anyString(), any(), any(), any());
        Pageable p = pageableCaptor.getValue();

        assertEquals(0, p.getPageNumber());
        Sort.Order order = p.getSort().getOrderFor("patientName");
        assertNotNull(order);
        assertEquals(Sort.Direction.ASC, order.getDirection());
    }

    @Test
    void deleteTestOrder_shouldDelegate_andWrapResponse_ascBranch() {
        PageResponse<TestOrderResponse> pageResponse = mock(PageResponse.class);

        when(testOrderService.deleteTestOrder(eq("o9"), any(Pageable.class), anyString(), any(), any(), any()))
                .thenReturn(pageResponse);

        RestResponse<PageResponse<TestOrderResponse>> resp =
                controller.deleteTestOrder(
                        "o9",
                        "",
                        null, null, null,
                        1, 6,
                        "patientName", "asc"
                );

        assertEquals(200, resp.getStatusCode());
        assertEquals("Test orders retrieved successfully", resp.getMessage());
        assertSame(pageResponse, resp.getResult());

        verify(testOrderService).deleteTestOrder(eq("o9"), any(Pageable.class), anyString(), any(), any(), any());
        verifyNoMoreInteractions(testOrderService, resyncRequestPublisher, testOrderRepository);
    }

    @Test
    void deleteTestOrder_descBranch_andPageLessThan1() {
        PageResponse<TestOrderResponse> pageResponse = mock(PageResponse.class);
        when(testOrderService.deleteTestOrder(eq("o9"), any(Pageable.class), anyString(), any(), any(), any()))
                .thenReturn(pageResponse);

        controller.deleteTestOrder(
                "o9",
                "k",
                LocalDate.parse("2025-01-01"),
                LocalDate.parse("2025-01-02"),
                TestOrderStatus.PENDING,
                0, 10,
                "createdAt", "DESC"
        );

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(testOrderService).deleteTestOrder(
                eq("o9"),
                pageableCaptor.capture(),
                eq("k"),
                eq(LocalDate.parse("2025-01-01")),
                eq(LocalDate.parse("2025-01-02")),
                eq(TestOrderStatus.PENDING)
        );

        Pageable p = pageableCaptor.getValue();
        assertEquals(0, p.getPageNumber());
        assertEquals(10, p.getPageSize());
        Sort.Order order = p.getSort().getOrderFor("createdAt");
        assertNotNull(order);
        assertEquals(Sort.Direction.DESC, order.getDirection());
    }

    @Test
    void getTestOrderStatistics_shouldDelegate() {
        RestResponse<?> serviceResp =
                RestResponse.builder()
                        .statusCode(200)
                        .message("stats")
                        .timestamp(LocalDateTime.now())
                        .result(new Object())
                        .build();

        doReturn(serviceResp).when(testOrderService).getTestOrderStatistics();

        RestResponse<?> resp = controller.getTestOrderStatistics();

        assertSame(serviceResp, resp);
        verify(testOrderService).getTestOrderStatistics();
        verifyNoMoreInteractions(testOrderService, resyncRequestPublisher, testOrderRepository);
    }

    @Test
    void getDailyStatistics_shouldDelegate() {
        RestResponse<?> serviceResp =
                RestResponse.builder()
                        .statusCode(200)
                        .message("daily")
                        .timestamp(LocalDateTime.now())
                        .result(new Object())
                        .build();

        doReturn(serviceResp).when(testOrderService).getDailyStatistics();

        RestResponse<?> resp = controller.getDailyStatistics();

        assertSame(serviceResp, resp);
        verify(testOrderService).getDailyStatistics();
        verifyNoMoreInteractions(testOrderService, resyncRequestPublisher, testOrderRepository);
    }

    @Test
    void reviewTestOrder_shouldDelegate() {
        RestResponse<TestOrderResponse> serviceResp =
                RestResponse.<TestOrderResponse>builder()
                        .statusCode(200)
                        .message("reviewed")
                        .timestamp(LocalDateTime.now())
                        .result(mock(TestOrderResponse.class))
                        .build();

        when(testOrderService.reviewTestOrder("o2")).thenReturn(serviceResp);

        RestResponse<TestOrderResponse> resp = controller.reviewTestOrder("o2");

        assertSame(serviceResp, resp);
        verify(testOrderService).reviewTestOrder("o2");
        verifyNoMoreInteractions(testOrderService, resyncRequestPublisher, testOrderRepository);
    }

    @Test
    void resyncTestOrderResults_whenOrderExists_shouldSendKafka_andReturn202() {
        TestOrder order = mock(TestOrder.class);
        when(order.getBloodCollectionId()).thenReturn("bc1");
        when(testOrderRepository.findById("o3")).thenReturn(Optional.of(order));

        RestResponse<Void> resp = controller.resyncTestOrderResults("o3");

        assertEquals(202, resp.getStatusCode());
        assertNotNull(resp.getTimestamp());
        assertTrue(String.valueOf(resp.getMessage()).contains("Resync request for orderId 'o3'"));

        verify(testOrderRepository).findById("o3");
        verify(resyncRequestPublisher).requestResync("o3", "bc1", "ManualTriggerByUser");
        verifyNoMoreInteractions(testOrderService, resyncRequestPublisher, testOrderRepository);
    }

    @Test
    void resyncTestOrderResults_whenOrderNotFound_shouldThrow() {
        when(testOrderRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> controller.resyncTestOrderResults("missing"));

        verify(testOrderRepository).findById("missing");
        verifyNoMoreInteractions(testOrderService, resyncRequestPublisher, testOrderRepository);
    }

    @Test
    void getTestOrderByEmail_shouldDelegate_andWrapResponse_ascBranch() {
        PageResponse<TestOrderDetailResponse> pageResponse = mock(PageResponse.class);

        when(testOrderService.getTestOrderByEmail(any(Pageable.class), eq("a@b.com")))
                .thenReturn(pageResponse);

        RestResponse<PageResponse<TestOrderDetailResponse>> resp =
                controller.getTestOrderByEmail("a@b.com", 1, 6, "createdBy", "asc");

        assertEquals(200, resp.getStatusCode());
        assertEquals("Test orders retrieved successfully", resp.getMessage());
        assertSame(pageResponse, resp.getResult());

        verify(testOrderService).getTestOrderByEmail(any(Pageable.class), eq("a@b.com"));
        verifyNoMoreInteractions(testOrderService, resyncRequestPublisher, testOrderRepository);
    }

    @Test
    void getTestOrderByEmail_descBranch_andPageLessThan1() {
        PageResponse<TestOrderDetailResponse> pageResponse = mock(PageResponse.class);

        when(testOrderService.getTestOrderByEmail(any(Pageable.class), eq("x@y.com")))
                .thenReturn(pageResponse);

        controller.getTestOrderByEmail("x@y.com", 0, 3, "createdAt", "DESC");

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(testOrderService).getTestOrderByEmail(pageableCaptor.capture(), eq("x@y.com"));

        Pageable p = pageableCaptor.getValue();
        assertEquals(0, p.getPageNumber());
        assertEquals(3, p.getPageSize());
        Sort.Order order = p.getSort().getOrderFor("createdAt");
        assertNotNull(order);
        assertEquals(Sort.Direction.DESC, order.getDirection());
    }

    @Test
    void getTestOrderByBloodCollectionId_shouldDelegate() {
        RestResponse<TestOrderResponse> serviceResp =
                RestResponse.<TestOrderResponse>builder()
                        .statusCode(200)
                        .message("ok")
                        .timestamp(LocalDateTime.now())
                        .result(mock(TestOrderResponse.class))
                        .build();

        when(testOrderService.getTestOrderByBloodCollectionId("bc9")).thenReturn(serviceResp);

        RestResponse<TestOrderResponse> resp = controller.getTestOrderByBloodCollectionId("bc9");

        assertSame(serviceResp, resp);
        verify(testOrderService).getTestOrderByBloodCollectionId("bc9");
        verifyNoMoreInteractions(testOrderService, resyncRequestPublisher, testOrderRepository);
    }
}
