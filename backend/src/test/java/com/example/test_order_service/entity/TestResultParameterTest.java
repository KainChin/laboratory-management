package com.example.test_order_service.entity;

import com.example.test_order_service.entity.enumForEntity.TestOrderStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TestResultParameterTest {

    private TestOrder validOrder() {
        TestOrder order = new TestOrder();
        order.setTestOrderId("to1");
        order.setPatientName("A");          // nullable=false
        order.setCitizenId("123");          // nullable=false
        order.setCountry("VN");             // nullable=false
        order.setBloodCollectionId("bc1");  // nullable=false
        order.setStatus(TestOrderStatus.PENDING); // nullable=false
        return order;
    }

    private TestResult validResult(TestOrder order) {
        TestResult result = new TestResult();
        result.setResultId("r1");
        result.setTestOrder(order);         // nullable=false (JoinColumn)
        result.setBloodCollectionId("bc1"); // nullable=false
        result.setHl7RawData("MSH|...");    // nullable=false
        return result;
    }

    @Test
    void gettersSettersAndEquality_shouldWork() {
        TestOrder order = validOrder();
        TestResult result = validResult(order);

        TestResultParameter a = new TestResultParameter();
        a.setId("p1");
        a.setTestResult(result);  // nullable=false
        a.setTestOrder(order);    // nullable=false
        a.setSequence(1);
        a.setObxIdentifier("OBX-3");
        a.setParamCode("GLU");
        a.setParamName("Glucose");
        a.setValue("5.0");
        a.setUnit("mmol/L");
        a.setRefRange("3.5-6.5");
        a.setFlag("N");
        a.setComputedBy("AI");

        TestResultParameter b = new TestResultParameter();
        b.setId("p1");
        b.setTestResult(result);
        b.setTestOrder(order);
        b.setSequence(1);
        b.setObxIdentifier("OBX-3");
        b.setParamCode("GLU");
        b.setParamName("Glucose");
        b.setValue("5.0");
        b.setUnit("mmol/L");
        b.setRefRange("3.5-6.5");
        b.setFlag("N");
        b.setComputedBy("AI");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotNull(a.toString());

        a.setFlag("H");
        assertEquals("H", a.getFlag());
        assertSame(order, a.getTestOrder());
        assertSame(result, a.getTestResult());
    }
}
