package com.example.test_order_service.mapper;

import com.example.test_order_service.dto.response.TestResultParameterResponse;
import com.example.test_order_service.entity.TestOrder;
import com.example.test_order_service.entity.TestResult;
import com.example.test_order_service.entity.TestResultParameter;
import com.example.test_order_service.entity.enumForEntity.TestOrderStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

class TestResultParameterMapperTest {

    private final TestResultParameterMapper mapper =
            Mappers.getMapper(TestResultParameterMapper.class);

    private TestOrder validOrder() {
        TestOrder o = new TestOrder();
        o.setTestOrderId("to1");
        o.setPatientName("A");
        o.setCitizenId("123");
        o.setCountry("VN");
        o.setBloodCollectionId("bc1");
        o.setStatus(TestOrderStatus.PENDING);
        return o;
    }

    private TestResult validResult(TestOrder order) {
        TestResult r = new TestResult();
        r.setResultId("r1");
        r.setTestOrder(order);
        r.setBloodCollectionId("bc1");
        r.setHl7RawData("MSH|...");
        return r;
    }

    @Test
    void toTestResultParameterResponse_shouldMapEntityToDto() {
        TestOrder order = validOrder();
        TestResult result = validResult(order);

        TestResultParameter p = new TestResultParameter();
        p.setId("p1");
        p.setTestOrder(order);
        p.setTestResult(result);
        p.setSequence(1);
        p.setParamCode("GLU");
        p.setParamName("Glucose");
        p.setValue("5.0");
        p.setUnit("mmol/L");
        p.setRefRange("3.5-6.5");
        p.setFlag("N");

        TestResultParameterResponse dto = mapper.toTestResultParameterResponse(p);

        assertNotNull(dto);
        assertEquals("p1", dto.getId());
        assertEquals(1, dto.getSequence());
        assertEquals("GLU", dto.getParamCode());
        assertEquals("Glucose", dto.getParamName());
        assertEquals("5.0", dto.getValue());
        assertEquals("mmol/L", dto.getUnit());
        assertEquals("3.5-6.5", dto.getRefRange());
        assertEquals("N", dto.getFlag());
    }
}
