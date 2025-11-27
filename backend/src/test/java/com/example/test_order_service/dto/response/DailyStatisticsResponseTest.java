package com.example.test_order_service.dto.response;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DailyStatisticsResponseTest {

    @Test
    void dailyDataBuilder_shouldWork() {
        DailyStatisticsResponse.DailyData d1 = DailyStatisticsResponse.DailyData.builder()
                .day("2025-11-25")
                .pending(1)
                .completed(2)
                .reviewed(3)
                .build();

        assertEquals("2025-11-25", d1.getDay());
        assertEquals(1, d1.getPending());
        assertEquals(2, d1.getCompleted());
        assertEquals(3, d1.getReviewed());

        DailyStatisticsResponse res = DailyStatisticsResponse.builder()
                .dailyData(List.of(d1))
                .build();

        assertEquals(1, res.getDailyData().size());
        assertEquals(d1, res.getDailyData().get(0));
    }

    @Test
    void lombokMethods_dailyData_shouldWork() {
        DailyStatisticsResponse.DailyData a =
                new DailyStatisticsResponse.DailyData("Mon", 1,2,3);
        DailyStatisticsResponse.DailyData b =
                new DailyStatisticsResponse.DailyData("Mon", 1,2,3);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotNull(a.toString());

        a.setReviewed(10);
        assertEquals(10, a.getReviewed());

        DailyStatisticsResponse s1 = new DailyStatisticsResponse(List.of(a));
        DailyStatisticsResponse s2 = new DailyStatisticsResponse(List.of(a));
        assertEquals(s1, s2);
        assertNotNull(s1.toString());
    }
}
