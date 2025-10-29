package com.example.test_order_service.event.dto;

import com.example.test_order_service.event.dto.payload.TestOrderCreatedPayload;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Event được publish khi một test order mới được tạo
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TestOrderCreatedEvent extends BaseEvent {
    
    /**
     * Dữ liệu chi tiết của test order
     */
    private TestOrderCreatedPayload payload;
}

