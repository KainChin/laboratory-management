package com.example.test_order_service.configuration;

import com.example.test_order_service.exception.AuthenticationEntryPointCustomizer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityConfigTest {

    // Dùng ApplicationContextRunner để load đúng bean SecurityConfig
    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withUserConfiguration(TestBeans.class)
                    .withBean(SecurityConfig.class);

    @Import(SecurityConfig.class)
    static class TestBeans {
        @Bean
        JwtDecoder jwtDecoder() {
            return mock(JwtDecoder.class);
        }

        @Bean
        AuthenticationEntryPointCustomizer authenticationEntryPointCustomizer() {
            return mock(AuthenticationEntryPointCustomizer.class);
        }
    }

    @Test
    void filterChainBean_shouldBeCreated() {
        contextRunner.run(ctx -> {
            assertTrue(ctx.containsBean("filterChain"));
            SecurityFilterChain chain = ctx.getBean(SecurityFilterChain.class);
            assertNotNull(chain);
        });
    }

    @Test
    void jwtAuthenticationConverter_shouldReadPrivilegesClaimWithoutPrefix() {
        SecurityConfig config = new SecurityConfig(
                mock(JwtDecoder.class),
                mock(AuthenticationEntryPointCustomizer.class)
        );

        JwtAuthenticationConverter converter = config.jwtAuthenticationConverter();

        Jwt jwt = new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(60),
                Map.of("alg", "none"),
                Map.of("privileges", List.of("ADMIN", "READ"))
        );

        var auth = converter.convert(jwt);
        assertNotNull(auth);

        var authorities = auth.getAuthorities();
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ADMIN")));
        assertTrue(authorities.contains(new SimpleGrantedAuthority("READ")));
    }

    @Test
    @SuppressWarnings("unchecked")
    void corsConfigurer_shouldRegisterExpectedCorsMapping() throws Exception {
        SecurityConfig config = new SecurityConfig(
                mock(JwtDecoder.class),
                mock(AuthenticationEntryPointCustomizer.class)
        );

        var corsConfigurer = config.corsConfigurer();

        CorsRegistry registry = new CorsRegistry();
        corsConfigurer.addCorsMappings(registry);

        // CorsRegistry giữ registrations private -> đọc bằng reflection
        Field regField = CorsRegistry.class.getDeclaredField("registrations");
        regField.setAccessible(true);
        List<Object> registrations = (List<Object>) regField.get(registry);

        assertNotNull(registrations);
        assertEquals(1, registrations.size());

        Object corsRegistration = registrations.get(0);

        // CorsRegistration có private field config -> đọc tiếp
        Field configField = corsRegistration.getClass().getDeclaredField("config");
        configField.setAccessible(true);
        Object corsConfig = configField.get(corsRegistration);

        // CorsConfiguration getters public
        var getAllowedOrigins = corsConfig.getClass().getMethod("getAllowedOrigins");
        var getAllowedMethods = corsConfig.getClass().getMethod("getAllowedMethods");
        var getAllowCredentials = corsConfig.getClass().getMethod("getAllowCredentials");
        var getMaxAge = corsConfig.getClass().getMethod("getMaxAge");

        List<String> origins = (List<String>) getAllowedOrigins.invoke(corsConfig);
        List<String> methods = (List<String>) getAllowedMethods.invoke(corsConfig);
        Boolean allowCreds = (Boolean) getAllowCredentials.invoke(corsConfig);
        Long maxAge = (Long) getMaxAge.invoke(corsConfig);

        assertTrue(origins.contains("http://18.141.34.176:5173"));
        assertTrue(origins.contains("http://localhost:5173"));

        assertTrue(methods.contains(HttpMethod.GET.name()));
        assertTrue(methods.contains(HttpMethod.POST.name()));
        assertTrue(methods.contains(HttpMethod.PUT.name()));
        assertTrue(methods.contains(HttpMethod.DELETE.name()));
        assertTrue(methods.contains(HttpMethod.OPTIONS.name()));
        assertTrue(methods.contains(HttpMethod.PATCH.name()));

        assertTrue(allowCreds);
        assertEquals(3600L, maxAge);
    }
}
