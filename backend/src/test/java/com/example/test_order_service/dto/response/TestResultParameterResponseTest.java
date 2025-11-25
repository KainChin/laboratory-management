package com.example.test_order_service.dto.response;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TestResultParameterResponseTest {

    @Test
    void builderAndGetters_shouldWork() {
        TestResultParameterResponse res = TestResultParameterResponse.builder()
                .id("p1")
                .sequence(1)
                .paramCode("GLU")
                .paramName("Glucose")
                .value("5.1")
                .unit("mmol/L")
                .refRange("3.5-6.5")
                .flag("N")
                .build();

        assertEquals("p1", res.getId());
        assertEquals(1, res.getSequence());
        assertEquals("GLU", res.getParamCode());
        assertEquals("Glucose", res.getParamName());
        assertEquals("5.1", res.getValue());
        assertEquals("mmol/L", res.getUnit());
        assertEquals("3.5-6.5", res.getRefRange());
        assertEquals("N", res.getFlag());
    }

    @Test
    void lombokGeneratedMethods_shouldWork() {
        TestResultParameterResponse a = new TestResultParameterResponse();
        TestResultParameterResponse b = new TestResultParameterResponse();

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotNull(a.toString());

        a.setFlag("H");
        assertEquals("H", a.getFlag());
    }
}
