package com.example.test_order_service.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UpdateCommentRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validRequest_shouldPassValidation() {
        UpdateCommentRequest req = UpdateCommentRequest.builder()
                .commentText("updated")
                .build();

        Set<ConstraintViolation<UpdateCommentRequest>> violations = validator.validate(req);
        assertTrue(violations.isEmpty());
    }

    @Test
    void blankComment_shouldFailValidation() {
        UpdateCommentRequest req = new UpdateCommentRequest("   ");
        Set<ConstraintViolation<UpdateCommentRequest>> violations = validator.validate(req);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("commentText")));
    }

    @Test
    void nullComment_shouldFailValidation() {
        UpdateCommentRequest req = new UpdateCommentRequest(null);
        Set<ConstraintViolation<UpdateCommentRequest>> violations = validator.validate(req);

        assertFalse(violations.isEmpty());
    }

    @Test
    void lombokGeneratedMethods_shouldWork() {
        UpdateCommentRequest a = new UpdateCommentRequest("x");
        UpdateCommentRequest b = new UpdateCommentRequest("x");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotNull(a.toString());

        a.setCommentText("z");
        assertEquals("z", a.getCommentText());
    }
}
