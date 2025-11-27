package com.example.test_order_service.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FlaggingConfigTest {

    static class ExposedFlaggingConfig extends FlaggingConfig {
        void triggerUpdate() { super.onUpdate(); }
    }

    @Test
    void onUpdate_shouldSetUpdatedAt() {
        ExposedFlaggingConfig cfg = new ExposedFlaggingConfig();
        assertNull(cfg.getUpdatedAt());

        cfg.triggerUpdate();

        assertNotNull(cfg.getUpdatedAt());
    }

    @Test
    void builderAndLombokMethods_shouldWork() {
        LocalDateTime now = LocalDateTime.now();

        FlaggingConfig a = FlaggingConfig.builder()
                .configId("cfg1")
                .parameter("GLU")
                .minValue(3.5)
                .maxValue(6.5)
                .unit("mmol/L")
                .updatedAt(now)
                .build();

        FlaggingConfig b = new FlaggingConfig("cfg1", "GLU", 3.5, 6.5, "mmol/L", now);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotNull(a.toString());

        a.setUnit("mg/dL");
        assertEquals("mg/dL", a.getUnit());
        assertEquals("GLU", a.getParameter());
    }
}
