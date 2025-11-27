package com.example.test_order_service.event.publisher;

import com.example.test_order_service.entity.TestOrder;
import com.example.test_order_service.entity.enumForEntity.TestOrderStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MonitoringEventPublisherTest {

    @Mock
    RabbitTemplate rabbitTemplate;

    @Mock
    ObjectMapper objectMapper;

    @InjectMocks
    MonitoringEventPublisher publisher;

    private TestOrder testOrder;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(publisher, "eventsEnabled", true);
        ReflectionTestUtils.setField(publisher, "exchangeName", "test-order-exchange");
        ReflectionTestUtils.setField(publisher, "rabbitTemplate", rabbitTemplate);

        testOrder = mock(TestOrder.class);

        // lenient để tránh UnnecessaryStubbing cho những test không dùng hết field
        lenient().when(testOrder.getTestOrderId()).thenReturn("order-1");
        lenient().when(testOrder.getPatientId()).thenReturn(1); // ✅ Integer, không phải "patient-1"
        lenient().when(testOrder.getPatientName()).thenReturn("John Doe");
        lenient().when(testOrder.getDateOfBirth()).thenReturn(null);
        lenient().when(testOrder.getCitizenId()).thenReturn("CID");
        lenient().when(testOrder.getCountry()).thenReturn("VN");
        lenient().when(testOrder.getGender()).thenReturn(null);
        lenient().when(testOrder.getPhone()).thenReturn("0123");
        lenient().when(testOrder.getAddress()).thenReturn("addr");
        lenient().when(testOrder.getEmail()).thenReturn("a@b.com");
        lenient().when(testOrder.getStatus()).thenReturn(TestOrderStatus.PENDING);
        lenient().when(testOrder.getCreatedBy()).thenReturn("user-1");
        lenient().when(testOrder.getCreatedAt()).thenReturn(LocalDateTime.now());
    }

    @Test
    void publishTestOrderCreated_eventsDisabled_shouldNotSend() {
        ReflectionTestUtils.setField(publisher, "eventsEnabled", false);

        publisher.publishTestOrderCreated(testOrder);

        verifyNoInteractions(objectMapper, rabbitTemplate);
    }

    @Test
    void publishTestOrderCreated_rabbitTemplateNull_shouldNotSend() {
        ReflectionTestUtils.setField(publisher, "rabbitTemplate", null);

        publisher.publishTestOrderCreated(testOrder);

        verifyNoInteractions(objectMapper);
        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    void publishTestOrderCreated_valid_shouldSerializeAndSend() throws Exception {
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"ok\":true}");

        publisher.publishTestOrderCreated(testOrder);

        ArgumentCaptor<String> exchangeCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> routingCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> messageCap = ArgumentCaptor.forClass(String.class);

        verify(rabbitTemplate, times(1))
                .convertAndSend(exchangeCap.capture(), routingCap.capture(), messageCap.capture());

        assertEquals("test-order-exchange", exchangeCap.getValue());
        assertEquals("test.order.created", routingCap.getValue());
        assertEquals("{\"ok\":true}", messageCap.getValue());

        verify(objectMapper, times(1)).writeValueAsString(any());
    }

    @Test
    void publishTestOrderCreated_objectMapperThrows_shouldNotCrash() throws Exception {
        when(objectMapper.writeValueAsString(any()))
                .thenThrow(new JsonProcessingException("boom") {});

        assertDoesNotThrow(() -> publisher.publishTestOrderCreated(testOrder));

        verify(objectMapper, times(1)).writeValueAsString(any());
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), anyString());
    }

    @Test
    void publishTestOrderCreated_rabbitThrows_shouldNotCrash() throws Exception {
        when(objectMapper.writeValueAsString(any())).thenReturn("msg");
        doThrow(new RuntimeException("rabbit down"))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), anyString());

        assertDoesNotThrow(() -> publisher.publishTestOrderCreated(testOrder));

        verify(objectMapper, times(1)).writeValueAsString(any());
        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), anyString());
    }

    @Test
    void publishStatusChanged_eventsDisabled_shouldNotSend() {
        ReflectionTestUtils.setField(publisher, "eventsEnabled", false);

        publisher.publishStatusChanged("order-1", TestOrderStatus.PENDING, TestOrderStatus.COMPLETED);

        verifyNoInteractions(objectMapper, rabbitTemplate);
    }

    @Test
    void publishStatusChanged_rabbitTemplateNull_shouldNotSend() {
        ReflectionTestUtils.setField(publisher, "rabbitTemplate", null);

        publisher.publishStatusChanged("order-1", TestOrderStatus.PENDING, TestOrderStatus.COMPLETED);

        verifyNoInteractions(objectMapper);
        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    void publishStatusChanged_valid_shouldSerializeAndSend() throws Exception {
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"status\":true}");

        publisher.publishStatusChanged("order-1", TestOrderStatus.PENDING, TestOrderStatus.COMPLETED);

        ArgumentCaptor<String> exchangeCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> routingCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> messageCap = ArgumentCaptor.forClass(String.class);

        verify(rabbitTemplate, times(1))
                .convertAndSend(exchangeCap.capture(), routingCap.capture(), messageCap.capture());

        assertEquals("test-order-exchange", exchangeCap.getValue());
        assertEquals("test.order.status.changed", routingCap.getValue());
        assertEquals("{\"status\":true}", messageCap.getValue());

        verify(objectMapper, times(1)).writeValueAsString(any());
    }

    @Test
    void publishStatusChanged_objectMapperThrows_shouldNotCrash() throws Exception {
        when(objectMapper.writeValueAsString(any()))
                .thenThrow(new JsonProcessingException("boom") {});

        assertDoesNotThrow(() ->
                publisher.publishStatusChanged("order-1", TestOrderStatus.PENDING, TestOrderStatus.COMPLETED)
        );

        verify(objectMapper, times(1)).writeValueAsString(any());
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), anyString());
    }

    @Test
    void publishStatusChanged_rabbitThrows_shouldNotCrash() throws Exception {
        when(objectMapper.writeValueAsString(any())).thenReturn("msg");
        doThrow(new RuntimeException("rabbit down"))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), anyString());

        assertDoesNotThrow(() ->
                publisher.publishStatusChanged("order-1", TestOrderStatus.PENDING, TestOrderStatus.COMPLETED)
        );

        verify(objectMapper, times(1)).writeValueAsString(any());
        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), anyString());
    }
}
