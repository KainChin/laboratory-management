package com.example.test_order_service.dto.response;

import com.example.test_order_service.entity.enumForEntity.Gender;
import com.example.test_order_service.entity.enumForEntity.TestOrderStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TestOrderResponseTest {

    @Test
    void builderAndGetters_shouldWork() {
        LocalDateTime now = LocalDateTime.now();
        TestOrderResponse res = TestOrderResponse.builder()
                .testOrderId("to1")
                .patientName("A")
                .patientId(1) // ✅ Integer, không phải "p1"
                .dateOfBirth(LocalDate.of(2000,1,1))
                .citizenId("123")
                .country("VN")
                .gender(Gender.FEMALE)
                .address("HN")
                .email("a@gmail.com")
                .phone("0123456789")
                .bloodCollectionId("bc1")
                .createdBy("admin")
                .createdAt(now)
                .status(TestOrderStatus.COMPLETED)
                .build();

        assertEquals("to1", res.getTestOrderId());
        assertEquals("A", res.getPatientName());
        assertEquals(1, res.getPatientId()); // ✅ thêm assert cho Integer
        assertEquals(Gender.FEMALE, res.getGender());
        assertEquals(TestOrderStatus.COMPLETED, res.getStatus());
        assertEquals(now, res.getCreatedAt());
    }

    @Test
    void lombokGeneratedMethods_shouldWork() {
        TestOrderResponse a = new TestOrderResponse();
        TestOrderResponse b = new TestOrderResponse();

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotNull(a.toString());

        a.setCountry("US");
        assertEquals("US", a.getCountry());
    }
}
