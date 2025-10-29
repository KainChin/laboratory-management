package com.example.test_order_service.event.dto;

import com.example.test_order_service.event.dto.payload.StatusChangedPayload;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Event được publish khi trạng thái của test order thay đổi
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TestOrderStatusChangedEvent extends BaseEvent {
    
    /**
     * Dữ liệu về sự thay đổi status
     */
    private StatusChangedPayload payload;
}

