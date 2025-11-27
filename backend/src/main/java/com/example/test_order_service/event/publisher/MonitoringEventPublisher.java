package com.example.test_order_service.event.publisher;

import com.example.test_order_service.entity.TestOrder;
import com.example.test_order_service.entity.enumForEntity.TestOrderStatus;
import com.example.test_order_service.event.dto.EventMetadata;
import com.example.test_order_service.event.dto.TestOrderCreatedEvent;
import com.example.test_order_service.event.dto.TestOrderStatusChangedEvent;
import com.example.test_order_service.event.dto.payload.StatusChangedPayload;
import com.example.test_order_service.event.dto.payload.TestOrderCreatedPayload;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service để publish events tới Monitoring Service
 * 
 * Đặc điểm an toàn:
 * - Nếu RabbitMQ không available, chỉ log warning, không throw exception
 * - Business logic vẫn chạy bình thường
 * - Có thể enable/disable qua config
 */
@Service
@Slf4j
public class MonitoringEventPublisher {
    
    @Autowired(required = false)
    private RabbitTemplate rabbitTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * Enable/disable event publishing
     * Default: false (an toàn)
     */
    @Value("${app.events.enabled:false}")
    private boolean eventsEnabled;
    
    /**
     * Exchange name cho test order events
     */
    @Value("${app.events.exchange-name:test-order-exchange}")
    private String exchangeName;
    
    /**
     * Publish event khi một test order mới được tạo
     */
    public void publishTestOrderCreated(TestOrder testOrder) {
        if (!shouldPublish()) {
            return;
        }
        
        try {
            TestOrderCreatedEvent event = buildTestOrderCreatedEvent(testOrder);
            publishEvent("test.order.created", event);
            log.info("✅ Published test.order.created for order: {}", testOrder.getTestOrderId());
        } catch (Exception e) {
            log.error("❌ Failed to publish test.order.created for order: {}", 
                     testOrder.getTestOrderId(), e);
            // KHÔNG throw exception - business logic tiếp tục
        }
    }
    
    /**
     * Publish event khi status của test order thay đổi
     */
    public void publishStatusChanged(String orderId, TestOrderStatus oldStatus, TestOrderStatus newStatus) {
        if (!shouldPublish()) {
            return;
        }
        
        try {
            TestOrderStatusChangedEvent event = buildStatusChangedEvent(orderId, oldStatus, newStatus);
            publishEvent("test.order.status.changed", event);
            log.info("✅ Published test.order.status.changed for order: {}", orderId);
        } catch (Exception e) {
            log.error("❌ Failed to publish status change event for order: {}", orderId, e);
            // KHÔNG throw exception
        }
    }
    
    /**
     * Check xem có nên publish event không
     */
    private boolean shouldPublish() {
        if (!eventsEnabled) {
            log.debug("📵 Event publishing is disabled");
            return false;
        }
        
        if (rabbitTemplate == null) {
            log.warn("⚠️  RabbitTemplate not available - events will not be published");
            return false;
        }
        
        return true;
    }
    
    /**
     * Publish event tới RabbitMQ
     */
    private void publishEvent(String routingKey, Object event) throws JsonProcessingException {
        String message = objectMapper.writeValueAsString(event);
        rabbitTemplate.convertAndSend(exchangeName, routingKey, message);
    }
    
    /**
     * Build TestOrderCreatedEvent từ TestOrder entity
     */
    private TestOrderCreatedEvent buildTestOrderCreatedEvent(TestOrder testOrder) {
        TestOrderCreatedPayload payload = TestOrderCreatedPayload.builder()
                .testOrderId(testOrder.getTestOrderId())
                .patientId(testOrder.getPatientId() != null ? testOrder.getPatientId() : null)
                .patientName(testOrder.getPatientName())
                .dateOfBirth(testOrder.getDateOfBirth())
                .citizenId(testOrder.getCitizenId())
                .country(testOrder.getCountry())
                .gender(testOrder.getGender() != null ? testOrder.getGender().toString() : null)
                .phone(testOrder.getPhone())
                .address(testOrder.getAddress())
                .email(testOrder.getEmail())
                .status(testOrder.getStatus())
                .createdBy(testOrder.getCreatedBy())
                .createdAt(testOrder.getCreatedAt())
                .build();
        
        EventMetadata metadata = EventMetadata.builder()
                .correlationId(UUID.randomUUID().toString())
                .traceId(UUID.randomUUID().toString())
                .userId(testOrder.getCreatedBy())
                .build();
        
        return TestOrderCreatedEvent.builder()
                .eventType("test.order.created")
                .eventId(UUID.randomUUID().toString())
                .timestamp(testOrder.getCreatedAt())
                .sourceService("test-order-service")
                .version("1.0")
                .metadata(metadata)
                .payload(payload)
                .build();
    }
    
    /**
     * Build TestOrderStatusChangedEvent
     */
    private TestOrderStatusChangedEvent buildStatusChangedEvent(
            String orderId, 
            TestOrderStatus oldStatus, 
            TestOrderStatus newStatus) {
        
        StatusChangedPayload payload = StatusChangedPayload.builder()
                .testOrderId(orderId)
                .previousStatus(oldStatus)
                .newStatus(newStatus)
                .build();
        
        EventMetadata metadata = EventMetadata.builder()
                .correlationId(UUID.randomUUID().toString())
                .traceId(UUID.randomUUID().toString())
                .build();
        
        return TestOrderStatusChangedEvent.builder()
                .eventType("test.order.status.changed")
                .eventId(UUID.randomUUID().toString())
                .timestamp(java.time.LocalDateTime.now())
                .sourceService("test-order-service")
                .version("1.0")
                .metadata(metadata)
                .payload(payload)
                .build();
    }
}

