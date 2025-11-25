package com.example.test_order_service.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CreateCommentRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validRequest_shouldPassValidation() {
        CreateCommentRequest req = CreateCommentRequest.builder()
                .commentText("hello")
                .build();

        Set<ConstraintViolation<CreateCommentRequest>> violations = validator.validate(req);
        assertTrue(violations.isEmpty());
    }

    @Test
    void blankComment_shouldFailValidation() {
        CreateCommentRequest req = new CreateCommentRequest("");
        Set<ConstraintViolation<CreateCommentRequest>> violations = validator.validate(req);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("commentText")));
    }

    @Test
    void nullComment_shouldFailValidation() {
        CreateCommentRequest req = new CreateCommentRequest(null);
        Set<ConstraintViolation<CreateCommentRequest>> violations = validator.validate(req);

        assertFalse(violations.isEmpty());
    }

    @Test
    void lombokGeneratedMethods_shouldWork() {
        CreateCommentRequest a = new CreateCommentRequest("x");
        CreateCommentRequest b = new CreateCommentRequest("x");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotNull(a.toString());

        a.setCommentText("y");
        assertEquals("y", a.getCommentText());
    }
}
