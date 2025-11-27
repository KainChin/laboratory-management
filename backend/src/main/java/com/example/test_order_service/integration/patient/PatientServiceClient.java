package com.example.test_order_service.integration.patient;

import com.example.test_order_service.config.PatientServiceProperties;
import com.example.test_order_service.dto.response.PatientDto;
import com.example.test_order_service.dto.response.PatientListResponse;
import com.example.test_order_service.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientServiceClient {
    private final PatientServiceProperties patientServiceProperties;
    private final WebClient.Builder webClientBuilder;

    /**
     * Get patient by ID from patient service
     * @param patientId Patient ID
     * @param authToken JWT token to forward to patient service
     * @return PatientDto
     * @throws ResourceNotFoundException if patient not found or inactive
     */
    public PatientDto getPatientById(Integer patientId, String authToken) {
        log.info("Fetching patient with ID: {} from patient service", patientId);

        WebClient webClient = webClientBuilder
                .baseUrl(patientServiceProperties.getBaseUrl())
                .build();

        try {
            PatientDto patient = webClient.get()
                    .uri("/api/patients/{id}", patientId)
                    .header(HttpHeaders.AUTHORIZATION, authToken)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, response -> {
                        if (response.statusCode().value() == 404) {
                            return Mono.error(new ResourceNotFoundException("Patient not found with ID: " + patientId));
                        }
                        return Mono.error(new RuntimeException("Error fetching patient: " + response.statusCode()));
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, response ->
                            Mono.error(new RuntimeException("Patient service error: " + response.statusCode())))
                    .bodyToMono(PatientDto.class)
                    .timeout(Duration.ofMillis(patientServiceProperties.getTimeoutMs()))
                    .block();

            if (patient == null) {
                throw new ResourceNotFoundException("Patient not found with ID: " + patientId);
            }

            // Validate patient is active
            if (patient.getIsActive() == null || !patient.getIsActive()) {
                throw new ResourceNotFoundException("Patient is inactive with ID: " + patientId);
            }

            log.info("Successfully fetched patient: {}", patient.getFullName());
            return patient;

        } catch (Exception e) {
            log.error("Error fetching patient with ID: {}", patientId, e);
            if (e instanceof ResourceNotFoundException) {
                throw e;
            }
            throw new RuntimeException("Failed to fetch patient from patient service", e);
        }
    }

    /**
     * Get all patients with pagination and search
     * @param page Page number (0-based)
     * @param pageSize Page size
     * @param keyword Search keyword
     * @param sort Sort parameter (e.g., "patientId,desc")
     * @param authToken JWT token to forward to patient service
     * @return PatientListResponse
     */
    public PatientListResponse getAllPatients(Integer page, Integer pageSize, String keyword, String sort, String authToken) {
        log.info("Fetching patients list - page: {}, pageSize: {}, keyword: {}", page, pageSize, keyword);

        WebClient webClient = webClientBuilder
                .baseUrl(patientServiceProperties.getBaseUrl())
                .build();

        try {
            return webClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path("/api/patients")
                                .queryParam("page", page != null ? page : 0)
                                .queryParam("pageSize", pageSize != null ? pageSize : 10)
                                .queryParam("sort", sort != null ? sort : "patientId,desc");

                        if (keyword != null && !keyword.trim().isEmpty()) {
                            uriBuilder.queryParam("keyword", keyword);
                        }

                        return uriBuilder.build();
                    })
                    .header(HttpHeaders.AUTHORIZATION, authToken)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, response ->
                            Mono.error(new RuntimeException("Error fetching patients: " + response.statusCode())))
                    .onStatus(HttpStatusCode::is5xxServerError, response ->
                            Mono.error(new RuntimeException("Patient service error: " + response.statusCode())))
                    .bodyToMono(PatientListResponse.class)
                    .timeout(Duration.ofMillis(patientServiceProperties.getTimeoutMs()))
                    .block();

        } catch (Exception e) {
            log.error("Error fetching patients list", e);
            throw new RuntimeException("Failed to fetch patients from patient service", e);
        }
    }
}

