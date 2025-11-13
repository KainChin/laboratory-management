package com.example.test_order_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyStatisticsResponse {
    private List<DailyData> dailyData;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailyData {
        private String day;
        private long pending;
        private long completed;
        private long reviewed;
    }
}

