package com.example.test_order_service.entity;

import com.example.test_order_service.entity.enumForEntity.TestOrderStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TestOrderTest {

    static class ExposedTestOrder extends TestOrder {
        void triggerCreate() { super.onCreate(); }
    }

    @Test
    void onCreate_shouldSetCreatedAtDeletedFalseAndStatusPending() {
        ExposedTestOrder o = new ExposedTestOrder();

        o.setDeleted(true);
        o.setStatus(TestOrderStatus.COMPLETED);

        assertNull(o.getCreatedAt());

        o.triggerCreate();

        assertNotNull(o.getCreatedAt());
        assertFalse(o.isDeleted());
        assertEquals(TestOrderStatus.PENDING, o.getStatus());
    }

    @Test
    void gettersSettersAndEquality_shouldWork() {
        TestOrder a = new TestOrder();
        a.setTestOrderId("to1");
        a.setPatientId(1); // ✅ Integer
        a.setPatientName("Nguyen Van A");
        a.setCitizenId("123");
        a.setCountry("VN");
        a.setBloodCollectionId("bc1");
        a.setStatus(TestOrderStatus.PENDING);

        TestOrder b = new TestOrder();
        b.setTestOrderId("to1");
        b.setPatientId(1); // ✅ Integer
        b.setPatientName("Nguyen Van A");
        b.setCitizenId("123");
        b.setCountry("VN");
        b.setBloodCollectionId("bc1");
        b.setStatus(TestOrderStatus.PENDING);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotNull(a.toString());

        a.setCountry("US");
        assertEquals("US", a.getCountry());
    }
}
