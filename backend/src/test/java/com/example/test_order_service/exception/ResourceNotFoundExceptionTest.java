package com.example.test_order_service.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResourceNotFoundExceptionTest {

    @Test
    void constructor_shouldSetMessage() {
        ResourceNotFoundException ex = new ResourceNotFoundException("missing");
        assertEquals("missing", ex.getMessage());
    }
}
