package com.example.test_order_service.event.dto.payload;

import com.example.test_order_service.entity.enumForEntity.TestOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload cho event test.order.status.changed
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusChangedPayload {
    
    private String testOrderId;
    private String patientName;
    private TestOrderStatus previousStatus;
    private TestOrderStatus newStatus;
    
    /**
     * Số lượng test results
     */
    private Integer totalResults;
    
    /**
     * Số lượng kết quả bất thường
     */
    private Integer abnormalResults;
    
    /**
     * Thông tin về người thực hiện (nếu có)
     */
    private String performedBy;
}

