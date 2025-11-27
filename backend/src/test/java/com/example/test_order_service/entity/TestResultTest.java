package com.example.test_order_service.entity;

import com.example.test_order_service.entity.enumForEntity.TestOrderStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TestResultTest {

    private TestOrder validOrder() {
        TestOrder order = new TestOrder();
        order.setTestOrderId("to1");
        order.setPatientName("A");
        order.setCitizenId("123");
        order.setCountry("VN");
        order.setBloodCollectionId("bc1");
        order.setStatus(TestOrderStatus.PENDING);
        return order;
    }

    @Test
    void gettersSettersAndEquality_shouldWork() {
        TestOrder order = validOrder();

        TestResultParameter p = new TestResultParameter();
        p.setId("p1");
        p.setParamCode("GLU");
        p.setValue("5.1");

        TestResult a = new TestResult();
        a.setResultId("r1");
        a.setTestOrder(order);           // nullable=false
        a.setPatientId("p1");
        a.setBloodCollectionId("bc1");   // nullable=false
        a.setInstrumentName("AU480");
        a.setHl7RawData("MSH|...");      // nullable=false
        a.setStatus("COMPLETED");
        a.setTestResultParameter(List.of(p));

        TestResult b = new TestResult(
                "r1", order, "p1", "bc1", "AU480",
                "MSH|...", "COMPLETED", List.of(p)
        );

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotNull(a.toString());

        a.setStatus("REVIEWED");
        assertEquals("REVIEWED", a.getStatus());
        assertSame(order, a.getTestOrder());
    }
}
