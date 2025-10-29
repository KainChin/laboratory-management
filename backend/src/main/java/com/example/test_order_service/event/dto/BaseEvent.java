package com.example.test_order_service.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Base class cho tất cả events trong hệ thống
 * Định nghĩa format chuẩn cho các events được publish
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEvent {
    
    /**
     * Loại event (vd: test.order.created, test.order.status.changed)
     */
    private String eventType;
    
    /**
     * Unique identifier cho event này
     */
    private String eventId;
    
    /**
     * Timestamp khi event được tạo
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    
    /**
     * Service nào đã tạo ra event này
     */
    private String sourceService;
    
    /**
     * Version của event schema
     */
    private String version;
    
    /**
     * Metadata cho event (correlationId, traceId, etc.)
     */
    private EventMetadata metadata;
}

