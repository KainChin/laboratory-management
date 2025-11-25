package com.example.test_order_service.dto.response;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FlaggingConfigResponseTest {

    @Test
    void builderAndGetters_shouldWork() {
        FlaggingConfigResponse res = FlaggingConfigResponse.builder()
                .configId("cfg1")
                .parameter("GLU")
                .minValue(3.5)
                .maxValue(6.5)
                .unit("mmol/L")
                .build();

        assertEquals("cfg1", res.getConfigId());
        assertEquals("GLU", res.getParameter());
        assertEquals(3.5, res.getMinValue());
        assertEquals(6.5, res.getMaxValue());
        assertEquals("mmol/L", res.getUnit());
    }

    @Test
    void lombokGeneratedMethods_shouldWork() {
        FlaggingConfigResponse a = new FlaggingConfigResponse("1","P",1.0,2.0,"u");
        FlaggingConfigResponse b = new FlaggingConfigResponse("1","P",1.0,2.0,"u");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotNull(a.toString());

        a.setUnit("mg/dL");
        assertEquals("mg/dL", a.getUnit());

        FlaggingConfigResponse empty = new FlaggingConfigResponse();
        empty.setConfigId("x");
        assertEquals("x", empty.getConfigId());
    }
}
