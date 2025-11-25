package com.example.test_order_service.dto.request;

import com.example.test_order_service.entity.enumForEntity.Gender;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TestOrderRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private TestOrderRequest validRequest() {
        return TestOrderRequest.builder()
                .patientName("Nguyen Van A")
                .dateOfBirth(LocalDate.now().minusYears(20))
                .citizenId("0123456789")
                .country("VN")
                .gender(Gender.MALE)
                .address("Hanoi")
                .email("a@gmail.com")
                .phone("0123456789")
                .build();
    }

    @Test
    void validRequest_shouldPassValidation() {
        TestOrderRequest req = validRequest();

        Set<ConstraintViolation<TestOrderRequest>> violations = validator.validate(req);
        assertTrue(violations.isEmpty());
    }

    @Test
    void blankPatientName_shouldFail() {
        TestOrderRequest req = validRequest();
        req.setPatientName(" ");

        Set<ConstraintViolation<TestOrderRequest>> violations = validator.validate(req);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("patientName")));
    }

    @Test
    void futureDateOfBirth_shouldFail() {
        TestOrderRequest req = validRequest();
        req.setDateOfBirth(LocalDate.now().plusDays(1));

        Set<ConstraintViolation<TestOrderRequest>> violations = validator.validate(req);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("dateOfBirth")));
    }

    @Test
    void blankCitizenId_shouldFail() {
        TestOrderRequest req = validRequest();
        req.setCitizenId("");

        Set<ConstraintViolation<TestOrderRequest>> violations = validator.validate(req);
        assertFalse(violations.isEmpty());
    }

    @Test
    void blankCountry_shouldFail() {
        TestOrderRequest req = validRequest();
        req.setCountry(null);

        Set<ConstraintViolation<TestOrderRequest>> violations = validator.validate(req);
        assertFalse(violations.isEmpty());
    }

    @Test
    void invalidEmail_shouldFail() {
        TestOrderRequest req = validRequest();
        req.setEmail("not-an-email");

        Set<ConstraintViolation<TestOrderRequest>> violations = validator.validate(req);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void invalidPhone_shouldFail() {
        TestOrderRequest req = validRequest();
        req.setPhone("123"); // sai regex

        Set<ConstraintViolation<TestOrderRequest>> violations = validator.validate(req);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("phone")));
    }

    @Test
    void lombokGeneratedMethods_shouldWork() {
        TestOrderRequest a = validRequest();
        TestOrderRequest b = validRequest();

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotNull(a.toString());

        // no-args constructor coverage
        TestOrderRequest empty = new TestOrderRequest();
        empty.setPatientName("X");
        assertEquals("X", empty.getPatientName());
    }
}
