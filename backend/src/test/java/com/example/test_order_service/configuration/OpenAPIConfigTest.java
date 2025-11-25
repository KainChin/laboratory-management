package com.example.test_order_service.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenAPIConfigTest {

    @Test
    void customOpenAPI_shouldContainBearerAuthSchemeAndRequirement() {
        OpenAPIConfig config = new OpenAPIConfig();

        OpenAPI openAPI = config.customOpenAPI();

        assertNotNull(openAPI);

        // security requirement
        assertNotNull(openAPI.getSecurity());
        assertFalse(openAPI.getSecurity().isEmpty());
        assertTrue(openAPI.getSecurity().stream()
                .anyMatch(sr -> sr.containsKey("bearerAuth")));

        // security scheme
        assertNotNull(openAPI.getComponents());
        assertNotNull(openAPI.getComponents().getSecuritySchemes());
        SecurityScheme scheme = openAPI.getComponents()
                .getSecuritySchemes()
                .get("bearerAuth");

        assertNotNull(scheme);
        assertEquals("Authorization", scheme.getName());
        assertEquals(SecurityScheme.Type.HTTP, scheme.getType());
        assertEquals("bearer", scheme.getScheme());
        assertEquals("JWT", scheme.getBearerFormat());
    }
}
