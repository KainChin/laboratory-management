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
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"rawtypes","unchecked"})
class PatientServiceClientTest {

    @Mock PatientServiceProperties properties;
    @Mock WebClient.Builder builder;
    @Mock WebClient webClient;

    @InjectMocks PatientServiceClient client;

    @BeforeEach
    void setUp() {
        when(properties.getBaseUrl()).thenReturn("http://patient-service");
        when(properties.getTimeoutMs()).thenReturn(2000);

        when(builder.baseUrl(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(webClient);
    }

    private void stubGetPatientChain(Mono<PatientDto> mono) {
        WebClient.RequestHeadersUriSpec uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString(), anyInt())).thenReturn(headersSpec);
        when(headersSpec.header(eq(HttpHeaders.AUTHORIZATION), anyString())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);

        when(responseSpec.onStatus(any(), any())).thenAnswer(inv -> {
            Predicate<HttpStatusCode> predicate = inv.getArgument(0);
            Function<ClientResponse, Mono<? extends Throwable>> handler = inv.getArgument(1);

            predicate.test(HttpStatus.NOT_FOUND);
            predicate.test(HttpStatus.BAD_REQUEST);
            predicate.test(HttpStatus.INTERNAL_SERVER_ERROR);

            ClientResponse resp404 = mock(ClientResponse.class);
            when(resp404.statusCode()).thenReturn(HttpStatus.NOT_FOUND);
            handler.apply(resp404);

            ClientResponse resp400 = mock(ClientResponse.class);
            when(resp400.statusCode()).thenReturn(HttpStatus.BAD_REQUEST);
            handler.apply(resp400);

            ClientResponse resp500 = mock(ClientResponse.class);
            when(resp500.statusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);
            handler.apply(resp500);

            return responseSpec;
        });

        when(responseSpec.bodyToMono(PatientDto.class)).thenReturn(mono);
    }

    private void stubGetAllPatientsChain(Mono<PatientListResponse> mono) {
        WebClient.RequestHeadersUriSpec uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(any(Function.class))).thenAnswer(inv -> {
            Function<UriBuilder, URI> fn = inv.getArgument(0);
            UriBuilder ub = new DefaultUriBuilderFactory().builder();
            fn.apply(ub);
            return headersSpec;
        });
        when(headersSpec.header(eq(HttpHeaders.AUTHORIZATION), anyString())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);

        when(responseSpec.onStatus(any(), any())).thenAnswer(inv -> {
            Predicate<HttpStatusCode> predicate = inv.getArgument(0);
            Function<ClientResponse, Mono<? extends Throwable>> handler = inv.getArgument(1);

            predicate.test(HttpStatus.BAD_REQUEST);
            predicate.test(HttpStatus.INTERNAL_SERVER_ERROR);

            ClientResponse resp400 = mock(ClientResponse.class);
            when(resp400.statusCode()).thenReturn(HttpStatus.BAD_REQUEST);
            handler.apply(resp400);

            ClientResponse resp500 = mock(ClientResponse.class);
            when(resp500.statusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);
            handler.apply(resp500);

            return responseSpec;
        });

        when(responseSpec.bodyToMono(PatientListResponse.class)).thenReturn(mono);
    }

    @Test
    void getPatientById_success_activePatient() {
        PatientDto patient = mock(PatientDto.class);
        when(patient.getIsActive()).thenReturn(true);
        when(patient.getFullName()).thenReturn("Nguyen Van A");

        stubGetPatientChain(Mono.just(patient));

        PatientDto res = client.getPatientById(1, "token");

        assertSame(patient, res);
        verify(builder).baseUrl("http://patient-service");
    }

    @Test
    void getPatientById_patientNull_shouldThrowNotFound() {
        stubGetPatientChain(Mono.empty());

        assertThrows(ResourceNotFoundException.class, () -> client.getPatientById(1, "token"));
    }

    @Test
    void getPatientById_inactivePatient_shouldThrowNotFound() {
        PatientDto patient = mock(PatientDto.class);
        when(patient.getIsActive()).thenReturn(false);

        stubGetPatientChain(Mono.just(patient));

        assertThrows(ResourceNotFoundException.class, () -> client.getPatientById(1, "token"));
    }

    @Test
    void getPatientById_activeNull_shouldThrowNotFound() {
        PatientDto patient = mock(PatientDto.class);
        when(patient.getIsActive()).thenReturn(null);

        stubGetPatientChain(Mono.just(patient));

        assertThrows(ResourceNotFoundException.class, () -> client.getPatientById(1, "token"));
    }

    @Test
    void getPatientById_bodyMonoThrowsResourceNotFound_shouldRethrow() {
        stubGetPatientChain(Mono.error(new ResourceNotFoundException("Patient not found with ID: 1")));

        assertThrows(ResourceNotFoundException.class, () -> client.getPatientById(1, "token"));
    }

    @Test
    void getPatientById_bodyMonoThrowsOther_shouldWrapRuntime() {
        stubGetPatientChain(Mono.error(new RuntimeException("boom")));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> client.getPatientById(1, "token"));
        assertTrue(ex.getMessage().contains("Failed to fetch patient from patient service"));
        assertNotNull(ex.getCause());
    }

    @Test
    void getAllPatients_success_withKeyword() {
        PatientListResponse listResp = mock(PatientListResponse.class);
        stubGetAllPatientsChain(Mono.just(listResp));

        PatientListResponse res = client.getAllPatients(0, 10, "abc", "patientId,desc", "token");

        assertSame(listResp, res);
        verify(builder).baseUrl("http://patient-service");
    }

    @Test
    void getAllPatients_success_withoutKeyword_andDefaults() {
        PatientListResponse listResp = mock(PatientListResponse.class);
        stubGetAllPatientsChain(Mono.just(listResp));

        PatientListResponse res = client.getAllPatients(null, null, "   ", null, "token");

        assertSame(listResp, res);
    }

    @Test
    void getAllPatients_success_nullKeyword() {
        PatientListResponse listResp = mock(PatientListResponse.class);
        stubGetAllPatientsChain(Mono.just(listResp));

        PatientListResponse res = client.getAllPatients(1, 20, null, "name,asc", "token");

        assertSame(listResp, res);
    }

    @Test
    void getAllPatients_bodyMonoError_shouldThrowRuntime() {
        stubGetAllPatientsChain(Mono.error(new RuntimeException("boom")));

        assertThrows(RuntimeException.class, () -> client.getAllPatients(0, 10, null, null, "token"));
    }
}
