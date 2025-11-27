package com.example.test_order_service.exception;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticationEntryPointCustomizerTest {

    private final AuthenticationEntryPointCustomizer entryPoint =
            new AuthenticationEntryPointCustomizer();

    @Test
    void commence_shouldSet401AndWriteRestResponseJson() throws Exception {
        // arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/test-orders");

        MockHttpServletResponse response = new MockHttpServletResponse();

        AuthenticationException authEx = new AuthenticationException("Unauthorized access") {};

        // act
        entryPoint.commence(request, response, authEx);

        // assert status + headers
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
        assertNotNull(response.getContentType());
        assertTrue(response.getContentType().startsWith("application/json"));
        assertEquals("UTF-8", response.getCharacterEncoding());

        // assert body json
        String body = response.getContentAsString(StandardCharsets.UTF_8);
        assertNotNull(body);
        assertFalse(body.isBlank());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(body);

        assertEquals(HttpStatus.UNAUTHORIZED.value(), root.get("statusCode").asInt());
        assertEquals(HttpStatus.UNAUTHORIZED.getReasonPhrase(), root.get("error").asText());
        assertEquals("/api/test-orders", root.get("path").asText());

        JsonNode msgNode = root.get("message");
        assertTrue(msgNode.isArray());
        assertEquals(1, msgNode.size());
        assertEquals("Unauthorized access", msgNode.get(0).asText());

        assertNotNull(root.get("timestamp"));
        assertFalse(root.get("timestamp").asText().isBlank());
    }

    @Test
    void commence_whenExceptionMessageNull_shouldThrowNullPointer() {
        // arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/x");

        MockHttpServletResponse response = new MockHttpServletResponse();

        AuthenticationException authEx = new AuthenticationException(null) {};

        // act + assert
        assertThrows(NullPointerException.class,
                () -> entryPoint.commence(request, response, authEx));
    }
}
