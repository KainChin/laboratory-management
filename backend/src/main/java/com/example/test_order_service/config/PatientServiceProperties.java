package com.example.test_order_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "patient-service")
public class PatientServiceProperties {
    private String baseUrl;
    private Integer timeoutMs;
}

