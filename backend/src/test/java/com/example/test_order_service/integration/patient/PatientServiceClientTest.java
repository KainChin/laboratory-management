package com.example.test_order_service.integration.patient;

import com.example.test_order_service.config.PatientServiceProperties;
import com.example.test_order_service.dto.response.PatientDto;
import com.example.test_order_service.dto.response.PatientListResponse;
import com.example.test_order_service.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"rawtypes","unchecked"})
class PatientServiceClientTest {

    @Mock PatientServiceProperties properties;
    @Mock WebClient.Builder builder;

    @Mock WebClient webClient;

    @Mock WebClient.RequestHeadersUriSpec reqUriSpec;
    @Mock WebClient.RequestHeadersSpec reqHeadersSpec;
    @Mock WebClient.ResponseSpec responseSpec;

    @InjectMocks PatientServiceClient client;

    @BeforeEach
    void setUp() {
        when(properties.getBaseUrl()).thenReturn("http://patient-service");
        when(properties.getTimeoutMs()).thenReturn(2000);

        when(builder.baseUrl(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(webClient);

        // ===== chain cho getPatientById (đánh lenient để không bị UnnecessaryStubbing) =====
        lenient().when(webClient.get()).thenReturn(reqUriSpec);
        lenient().when(reqUriSpec.uri(anyString(), anyInt())).thenReturn(reqHeadersSpec);
        lenient().when(reqHeadersSpec.header(eq(HttpHeaders.AUTHORIZATION), anyString()))
                .thenReturn(reqHeadersSpec);
        lenient().when(reqHeadersSpec.retrieve()).thenReturn(responseSpec);
        lenient().when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
    }

    // ===================== getPatientById =====================

    @Test
    void getPatientById_success_activePatient() {
        PatientDto patient = mock(PatientDto.class);
        when(patient.getIsActive()).thenReturn(true);
        when(patient.getFullName()).thenReturn("Nguyen Van A");

        when(responseSpec.bodyToMono(PatientDto.class)).thenReturn(Mono.just(patient));

        PatientDto res = client.getPatientById(1, "token");

        assertSame(patient, res);

        verify(builder).baseUrl("http://patient-service");
        verify(reqUriSpec).uri("/api/patients/{id}", 1);
        verify(reqHeadersSpec).header(HttpHeaders.AUTHORIZATION, "token");
    }

    @Test
    void getPatientById_patientNull_shouldThrowNotFound() {
        when(responseSpec.bodyToMono(PatientDto.class)).thenReturn(Mono.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> client.getPatientById(1, "token"));
    }

    @Test
    void getPatientById_inactivePatient_shouldThrowNotFound() {
        PatientDto patient = mock(PatientDto.class);
        when(patient.getIsActive()).thenReturn(false);

        when(responseSpec.bodyToMono(PatientDto.class)).thenReturn(Mono.just(patient));

        assertThrows(ResourceNotFoundException.class,
                () -> client.getPatientById(1, "token"));
    }

    @Test
    void getPatientById_activeNull_shouldThrowNotFound() {
        PatientDto patient = mock(PatientDto.class);
        when(patient.getIsActive()).thenReturn(null);

        when(responseSpec.bodyToMono(PatientDto.class)).thenReturn(Mono.just(patient));

        assertThrows(ResourceNotFoundException.class,
                () -> client.getPatientById(1, "token"));
    }

    @Test
    void getPatientById_serviceNotFound_shouldThrowNotFound() {
        when(responseSpec.bodyToMono(PatientDto.class))
                .thenReturn(Mono.error(new ResourceNotFoundException("Patient not found with ID: 1")));

        assertThrows(ResourceNotFoundException.class,
                () -> client.getPatientById(1, "token"));
    }

    @Test
    void getPatientById_otherError_shouldThrowRuntime() {
        when(responseSpec.bodyToMono(PatientDto.class))
                .thenReturn(Mono.error(new RuntimeException("boom")));

        assertThrows(RuntimeException.class,
                () -> client.getPatientById(1, "token"));
    }

    // ===================== getAllPatients =====================

    @Test
    void getAllPatients_success() {
        PatientListResponse listResp = mock(PatientListResponse.class);

        WebClient.RequestHeadersUriSpec listUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec listHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec listResponseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.get()).thenReturn(listUriSpec);

        when(listUriSpec.uri(any(Function.class))).thenReturn(listHeadersSpec);
        when(listHeadersSpec.header(eq(HttpHeaders.AUTHORIZATION), anyString()))
                .thenReturn(listHeadersSpec);
        when(listHeadersSpec.retrieve()).thenReturn(listResponseSpec);
        when(listResponseSpec.onStatus(any(), any())).thenReturn(listResponseSpec);
        when(listResponseSpec.bodyToMono(PatientListResponse.class))
                .thenReturn(Mono.just(listResp));

        PatientListResponse res =
                client.getAllPatients(0, 10, "abc", "patientId,desc", "token");

        assertSame(listResp, res);
        verify(listHeadersSpec).header(HttpHeaders.AUTHORIZATION, "token");
    }

    @Test
    void getAllPatients_error_shouldThrowRuntime() {
        WebClient.RequestHeadersUriSpec listUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec listHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec listResponseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.get()).thenReturn(listUriSpec);
        when(listUriSpec.uri(any(Function.class))).thenReturn(listHeadersSpec);
        when(listHeadersSpec.header(eq(HttpHeaders.AUTHORIZATION), anyString()))
                .thenReturn(listHeadersSpec);
        when(listHeadersSpec.retrieve()).thenReturn(listResponseSpec);
        when(listResponseSpec.onStatus(any(), any())).thenReturn(listResponseSpec);
        when(listResponseSpec.bodyToMono(PatientListResponse.class))
                .thenReturn(Mono.error(new RuntimeException("boom")));

        assertThrows(RuntimeException.class,
                () -> client.getAllPatients(0, 10, null, null, "token"));
    }
}
