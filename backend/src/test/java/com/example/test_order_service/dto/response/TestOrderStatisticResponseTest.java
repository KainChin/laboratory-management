package com.example.test_order_service.dto.response;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TestOrderStatisticResponseTest {

    @Test
    void builderAndGetters_shouldWork() {
        TestOrderStatisticResponse res = TestOrderStatisticResponse.builder()
                .total(10)
                .pending(2)
                .completed(5)
                .cancelled(1)
                .reviewed(2)
                .aiReviewed(3)
                .build();

        assertEquals(10, res.getTotal());
        assertEquals(2, res.getPending());
        assertEquals(5, res.getCompleted());
        assertEquals(1, res.getCancelled());
        assertEquals(2, res.getReviewed());
        assertEquals(3, res.getAiReviewed());
    }

    @Test
    void lombokGeneratedMethods_shouldWork() {
        TestOrderStatisticResponse a = new TestOrderStatisticResponse(1,2,3,4,5,6);
        TestOrderStatisticResponse b = new TestOrderStatisticResponse(1,2,3,4,5,6);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotNull(a.toString());

        a.setTotal(99);
        assertEquals(99, a.getTotal());
    }
}
