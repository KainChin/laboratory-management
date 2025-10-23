package com.example.test_order_service.dto.repsonse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestOrderStatisticResponse {
    private long total;
    private long pending;
    private long completed;
    private long cancelled;
    private long reviewed;
    private long aiReviewed;
}
