package com.example.test_order_service.dto.response;

import com.example.test_order_service.entity.enumForEntity.Gender;
import com.example.test_order_service.entity.enumForEntity.TestOrderStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TestOrderDetailResponseTest {

    @Test
    void builderAndGetters_shouldWork() {
        TestResultResponse testResult = TestResultResponse.builder()
                .bloodCollectionId("bc1")
                .instrumentName("Inst")
                .status("DONE")
                .hl7RawData("MSH|...")
                .testResultParameter(List.of())
                .build();

        CommentResponse comment = CommentResponse.builder()
                .commentId("c1")
                .commentText("note")
                .build();

        LocalDateTime now = LocalDateTime.now();

        TestOrderDetailResponse res = TestOrderDetailResponse.builder()
                .testOrderId("to1")
                .patientId("p1")
                .patientName("A")
                .dateOfBirth(LocalDate.of(2000, 1, 1))
                .age(25)
                .citizenId("123")
                .country("VN")
                .gender(Gender.MALE)
                .phone("0123456789")
                .address("HN")
                .email("a@gmail.com")
                .status(TestOrderStatus.PENDING)
                .createdBy("admin")
                .createdAt(now)
                .bloodCollectionId("bc1")
                .testResults(testResult)
                .comments(List.of(comment))
                .build();

        assertEquals("to1", res.getTestOrderId());
        assertEquals("p1", res.getPatientId());
        assertEquals("A", res.getPatientName());
        assertEquals(25, res.getAge());
        assertEquals(Gender.MALE, res.getGender());
        assertEquals(TestOrderStatus.PENDING, res.getStatus());
        assertEquals(testResult, res.getTestResults());
        assertEquals(1, res.getComments().size());
    }

    @Test
    void lombokGeneratedMethods_shouldWork() {
        TestOrderDetailResponse a = new TestOrderDetailResponse();
        TestOrderDetailResponse b = new TestOrderDetailResponse();

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotNull(a.toString());

        a.setTestOrderId("x");
        assertEquals("x", a.getTestOrderId());
    }
}
