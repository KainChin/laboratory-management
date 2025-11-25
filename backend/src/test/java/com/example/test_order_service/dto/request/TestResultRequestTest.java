package com.example.test_order_service.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TestResultRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void validHl7RawData_shouldPass() {
        TestResultRequest req = new TestResultRequest();
        setField(req, "hl7RawData", "MSH|^~\\&|...");

        Set<ConstraintViolation<TestResultRequest>> violations = validator.validate(req);
        assertTrue(violations.isEmpty());
    }

    @Test
    void blankHl7RawData_shouldFail() {
        TestResultRequest req = new TestResultRequest();
        setField(req, "hl7RawData", "");

        Set<ConstraintViolation<TestResultRequest>> violations = validator.validate(req);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("hl7RawData")));
    }

    @Test
    void nullHl7RawData_shouldFail() {
        TestResultRequest req = new TestResultRequest(); // hl7RawData mặc định null

        Set<ConstraintViolation<TestResultRequest>> violations = validator.validate(req);
        assertFalse(violations.isEmpty());
    }
}
