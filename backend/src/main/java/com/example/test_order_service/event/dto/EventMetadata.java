package com.example.test_order_service.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Metadata cho events
 * Chứa thông tin để tracing và correlation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventMetadata {
    
    /**
     * Correlation ID để tracking request across services
     */
    private String correlationId;
    
    /**
     * Trace ID cho distributed tracing
     */
    private String traceId;
    
    /**
     * User ID của người thực hiện action
     */
    private String userId;
    
    /**
     * IP address của client
     */
    private String clientIp;
}

