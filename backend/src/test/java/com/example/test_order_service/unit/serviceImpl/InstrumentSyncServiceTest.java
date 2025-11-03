package com.example.test_order_service.unit.serviceImpl;

import com.example.test_order_service.entity.TestOrder;
import com.example.test_order_service.entity.enumForEntity.TestOrderStatus;
import com.example.test_order_service.integration.instrument.InstrumentClient;
import com.example.test_order_service.repository.TestOrderRepository;
import com.example.test_order_service.serviceImpl.InstrumentSyncService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.scheduling.TaskScheduler;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class InstrumentSyncServiceTest {

    @Mock
    private InstrumentClient instrumentClient;

    @Mock
    private TestOrderRepository testOrderRepository;

    @Mock
    private TaskScheduler taskScheduler;

    @InjectMocks
    private InstrumentSyncService instrumentSyncService;

    private TestOrder testOrder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testOrder = new TestOrder();
        testOrder.setTestOrderId("ORDER-1");
        testOrder.setStatus(TestOrderStatus.PENDING);
        when(testOrderRepository.findById("ORDER-1")).thenReturn(Optional.of(testOrder));
        // By default, TaskScheduler will be verified by interactions; no real scheduling
    }

    @Test
    void requestOrWait_shouldComplete_whenPayloadReturned() {
        when(instrumentClient.requestResults("ORDER-1")).thenReturn("{\"ok\":true}");

        instrumentSyncService.requestOrWait("ORDER-1");

        // Verify status updated to COMPLETED and saved
        ArgumentCaptor<TestOrder> captor = ArgumentCaptor.forClass(TestOrder.class);
        verify(testOrderRepository, atLeastOnce()).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(TestOrderStatus.COMPLETED);

        // Should NOT schedule polling if payload available
        verify(taskScheduler, never()).scheduleAtFixedRate(any(Runnable.class), any(Instant.class), any(Duration.class));
    }

    @Test
    void requestOrWait_shouldWaitAndSchedule_whenClientThrows() {
        when(instrumentClient.requestResults("ORDER-1")).thenThrow(new RuntimeException("timeout"));

        instrumentSyncService.requestOrWait("ORDER-1");

        // Verify status updated to WAITING_FOR_INSTRUMENT
        ArgumentCaptor<TestOrder> captor = ArgumentCaptor.forClass(TestOrder.class);
        verify(testOrderRepository, atLeastOnce()).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(TestOrderStatus.WAITING_FOR_INSTRUMENT);

        // Verify scheduling happened
        verify(taskScheduler, times(1)).scheduleAtFixedRate(any(Runnable.class), any(Instant.class), any(Duration.class));
    }
}


