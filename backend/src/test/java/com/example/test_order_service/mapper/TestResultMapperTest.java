package com.example.test_order_service.mapper;

import com.example.test_order_service.dto.response.TestResultResponse;
import com.example.test_order_service.entity.TestOrder;
import com.example.test_order_service.entity.TestResult;
import com.example.test_order_service.entity.enumForEntity.TestOrderStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TestResultMapperTest {

    private final TestResultMapper mapper = Mappers.getMapper(TestResultMapper.class);

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

    @Test
    void toTestResultResponse_shouldMapEntityToDto() {
        TestResult r = new TestResult();
        r.setResultId("r1");
        r.setTestOrder(validOrder());
        r.setBloodCollectionId("bc1");
        r.setInstrumentName("AU480");
        r.setHl7RawData("MSH|...");
        r.setStatus("COMPLETED");

        TestResultResponse dto = mapper.toTestResultResponse(r);

        assertNotNull(dto);
        assertEquals("bc1", dto.getBloodCollectionId());
        assertEquals("AU480", dto.getInstrumentName());
        assertEquals("MSH|...", dto.getHl7RawData());
        assertEquals("COMPLETED", dto.getStatus());
    }

    @Test
    void toTestResultResponses_shouldMapList() {
        TestResult r1 = new TestResult();
        r1.setResultId("r1");
        r1.setTestOrder(validOrder());
        r1.setBloodCollectionId("bc1");
        r1.setHl7RawData("x");

        TestResult r2 = new TestResult();
        r2.setResultId("r2");
        r2.setTestOrder(validOrder());
        r2.setBloodCollectionId("bc2");
        r2.setHl7RawData("y");

        List<TestResultResponse> dtos = mapper.toTestResultResponses(List.of(r1, r2));

        assertEquals(2, dtos.size());
        assertEquals("bc1", dtos.get(0).getBloodCollectionId());
        assertEquals("bc2", dtos.get(1).getBloodCollectionId());
    }
}
