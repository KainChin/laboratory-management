package com.example.test_order_service.dto.response;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class RestResponseTest {

    @Test
    void fullConstructorAndBuilder_shouldWork() {
        LocalDateTime now = LocalDateTime.now();

        RestResponse<String> res = RestResponse.<String>builder()
                .statusCode(200)
                .message("OK")
                .result("data")
                .error(null)
                .timestamp(now)
                .path("/api/test")
                .build();

        assertEquals(200, res.getStatusCode());
        assertEquals("OK", res.getMessage());
        assertEquals("data", res.getResult());
        assertNull(res.getError());
        assertEquals(now, res.getTimestamp());
        assertEquals("/api/test", res.getPath());
    }

    @Test
    void customConstructor_shouldSetOnlyThreeFields() {
        RestResponse<String> res = new RestResponse<>(201, "Created", "x");

        assertEquals(201, res.getStatusCode());
        assertEquals("Created", res.getMessage());
        assertEquals("x", res.getResult());
        assertNull(res.getError());
        assertNull(res.getTimestamp());
        assertNull(res.getPath());
    }

    @Test
    void lombokGeneratedMethods_shouldWork() {
        RestResponse<Integer> a = new RestResponse<>(200, "ok", 1, null, null, null);
        RestResponse<Integer> b = new RestResponse<>(200, "ok", 1, null, null, null);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotNull(a.toString());

        a.setError("err");
        assertEquals("err", a.getError());
    }
}
