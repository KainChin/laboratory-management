package com.example.test_order_service.event.dto.payload;

import com.example.test_order_service.entity.enumForEntity.TestOrderStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Payload cho event test.order.created
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestOrderCreatedPayload {
    
    private String testOrderId;
    private String patientId;
    private String patientName;
    
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dateOfBirth;
    
    private String citizenId;
    private String country;
    private String gender;
    private String phone;
    private String address;
    private String email;
    private TestOrderStatus status;
    private String createdBy;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}

