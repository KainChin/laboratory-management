package com.example.test_order_service.dto.response;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TestResultResponseTest {

    @Test
    void builderAndGetters_shouldWork() {
        TestResultParameterResponse p = TestResultParameterResponse.builder()
                .id("1").paramCode("GLU").value("5.0").build();

        TestResultResponse res = TestResultResponse.builder()
                .bloodCollectionId("bc1")
                .instrumentName("AU480")
                .status("DONE")
                .hl7RawData("MSH|...")
                .testResultParameter(List.of(p))
                .build();

        assertEquals("bc1", res.getBloodCollectionId());
        assertEquals("AU480", res.getInstrumentName());
        assertEquals("DONE", res.getStatus());
        assertEquals("MSH|...", res.getHl7RawData());
        assertEquals(1, res.getTestResultParameter().size());
    }

    @Test
    void lombokGeneratedMethods_shouldWork() {
        TestResultResponse a = new TestResultResponse();
        TestResultResponse b = new TestResultResponse();

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotNull(a.toString());

        a.setStatus("PENDING");
        assertEquals("PENDING", a.getStatus());
    }
}
