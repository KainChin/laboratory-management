package com.example.test_order_service.dto.request;

import com.example.test_order_service.entity.enumForEntity.Gender;
import com.example.test_order_service.entity.enumForEntity.TestOrderStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TestOrderUpdateRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private TestOrderUpdateRequest validRequest() {
        return TestOrderUpdateRequest.builder()
                .patientName("Nguyen Van B")
                .dateOfBirth(LocalDate.now().minusYears(10))
                .citizenId("999999999")
                .country("VN")
                .gender(Gender.FEMALE)
                .status(TestOrderStatus.PENDING)
                .phone("0123456789")
                .address("HCM")
                .email("b@gmail.com")
                .build();
    }

    @Test
    void validRequest_shouldPassValidation() {
        TestOrderUpdateRequest req = validRequest();

        Set<ConstraintViolation<TestOrderUpdateRequest>> violations = validator.validate(req);
        assertTrue(violations.isEmpty());
    }

    @Test
    void invalidPhone_shouldFail() {
        TestOrderUpdateRequest req = validRequest();
        req.setPhone("abcxyz");

        Set<ConstraintViolation<TestOrderUpdateRequest>> violations = validator.validate(req);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("phone")));
    }

    @Test
    void invalidEmail_shouldFail() {
        TestOrderUpdateRequest req = validRequest();
        req.setEmail("wrong_email");

        Set<ConstraintViolation<TestOrderUpdateRequest>> violations = validator.validate(req);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void nullOptionalFields_shouldStillPass() {
        TestOrderUpdateRequest req = new TestOrderUpdateRequest();
        // class này không @NotBlank gì nên null toàn bộ vẫn hợp lệ
        Set<ConstraintViolation<TestOrderUpdateRequest>> violations = validator.validate(req);
        assertTrue(violations.isEmpty());
    }

    @Test
    void lombokGeneratedMethods_shouldWork() {
        TestOrderUpdateRequest a = validRequest();
        TestOrderUpdateRequest b = validRequest();

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotNull(a.toString());

        a.setCountry("US");
        assertEquals("US", a.getCountry());
    }
}
