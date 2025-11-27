package com.example.test_order_service.exception;

import com.example.test_order_service.dto.response.RestResponse;
import com.example.test_order_service.entity.enumForEntity.Gender;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private HttpServletRequest mockRequest(String uri) {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestURI()).thenReturn(uri);
        return req;
    }

    // ---------- RuntimeException ----------
    @Test
    void handleRuntimeException_shouldReturnBadRequest() {
        HttpServletRequest req = mockRequest("/api/x");
        RuntimeException ex = new RuntimeException("boom");

        ResponseEntity<RestResponse> res = handler.handleRuntimeException(ex, req);

        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        assertEquals(400, res.getBody().getStatusCode());
        assertEquals("Bad Request", res.getBody().getError());
        assertEquals("boom", res.getBody().getMessage());
        assertEquals("/api/x", res.getBody().getPath());
        assertNotNull(res.getBody().getTimestamp());
    }

    // ---------- MethodArgumentNotValidException ----------
    @Test
    void handleMethodArgumentNotValidException_shouldCollectFieldErrors() {
        HttpServletRequest req = mockRequest("/api/validate");

        Object target = new Object();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "target");
        bindingResult.addError(new FieldError("target", "patientName", "Patient name is required"));
        bindingResult.addError(new FieldError("target", "phone", "Phone number is invalid"));

        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<RestResponse> res = handler.handleMethodArgumentNotValidException(ex, req);

        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        assertEquals(List.of("Patient name is required", "Phone number is invalid"),
                res.getBody().getMessage());
        assertEquals("/api/validate", res.getBody().getPath());
    }

    // ---------- HttpMessageNotReadableException: LocalDate invalid ----------
    @Test
    void handleHttpMessageNotReadableException_localDate_shouldReturnCustomDateMessage() {
        HttpServletRequest req = mockRequest("/api/date");

        InvalidFormatException ifx =
                InvalidFormatException.from(null, "bad date", "32/13/9999", LocalDate.class);
        ifx.prependPath(new Object(), "dateOfBirth");

        HttpMessageNotReadableException ex = new HttpMessageNotReadableException(
                "msg", ifx, dummyInputMessage()
        );

        ResponseEntity<RestResponse> res = handler.handleHttpMessageNotReadableException(ex, req);

        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        assertEquals(List.of("Birthdate must in dd/MM/yyyy format"), res.getBody().getMessage());
    }

    // ---------- HttpMessageNotReadableException: Enum invalid ----------
    @Test
    void handleHttpMessageNotReadableException_enum_shouldReturnEnumMessage() {
        HttpServletRequest req = mockRequest("/api/enum");

        InvalidFormatException ifx =
                InvalidFormatException.from(null, "bad enum", "X", Gender.class);
        ifx.prependPath(new Object(), "gender");

        HttpMessageNotReadableException ex = new HttpMessageNotReadableException(
                "msg", ifx, dummyInputMessage()
        );

        ResponseEntity<RestResponse> res = handler.handleHttpMessageNotReadableException(ex, req);

        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());

        // message dạng: "Gender must be one of: MALE, FEMALE, ..."
        List<?> msgs = (List<?>) res.getBody().getMessage();
        assertEquals(1, msgs.size());
        assertTrue(msgs.get(0).toString().startsWith("Gender must be one of:"));
        assertTrue(msgs.get(0).toString().contains("MALE"));
    }

    // ---------- HttpMessageNotReadableException: JSON syntax/other ----------
    @Test
    void handleHttpMessageNotReadableException_otherCause_shouldReturnGenericJsonMessage() {
        HttpServletRequest req = mockRequest("/api/json");

        HttpMessageNotReadableException ex = new HttpMessageNotReadableException(
                "invalid json", new RuntimeException("oops"), dummyInputMessage()
        );

        ResponseEntity<RestResponse> res = handler.handleHttpMessageNotReadableException(ex, req);

        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        assertEquals(List.of("Invalid JSON format. Please check the data structure."),
                res.getBody().getMessage());
    }

    // ---------- ResourceNotFoundException ----------
    @Test
    void handleResourceNotFoundException_shouldReturn404() {
        HttpServletRequest req = mockRequest("/api/notfound");
        ResourceNotFoundException ex = new ResourceNotFoundException("not found");

        ResponseEntity<RestResponse<Void>> res = handler.handleResourceNotFoundException(ex, req);

        assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
        assertEquals(404, res.getBody().getStatusCode());
        assertEquals(List.of("not found"), res.getBody().getMessage());
    }

    // ---------- IllegalArgumentException ----------
    @Test
    void handleIllegalArgumentException_shouldReturn400() {
        HttpServletRequest req = mockRequest("/api/illegal-arg");
        IllegalArgumentException ex = new IllegalArgumentException("bad arg");

        ResponseEntity<RestResponse<Void>> res = handler.handleIllegalArgumentException(ex, req);

        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        assertEquals(List.of("bad arg"), res.getBody().getMessage());
    }

    // ---------- IllegalStateException ----------
    @Test
    void handleIllegalStateException_shouldReturn400() {
        HttpServletRequest req = mockRequest("/api/illegal-state");
        IllegalStateException ex = new IllegalStateException("bad state");

        ResponseEntity<RestResponse<Void>> res = handler.handleIllegalStateException(ex, req);

        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        assertEquals(List.of("bad state"), res.getBody().getMessage());
    }

    // ---------- MethodArgumentTypeMismatchException ----------
    @Test
    void handleMethodArgumentTypeMismatchException_localDate_shouldUseExpectedTypeYyyyMmDd() {
        HttpServletRequest req = mockRequest("/api/mismatch");

        MethodArgumentTypeMismatchException ex =
                new MethodArgumentTypeMismatchException(
                        "abc", LocalDate.class, "dateOfBirth", null, new IllegalArgumentException()
                );

        ResponseEntity<RestResponse<Void>> res =
                handler.handleMethodArgumentTypeMismatchException(ex, req);

        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());

        String msg = ((List<String>) res.getBody().getMessage()).get(0);
        assertTrue(msg.contains("Expected type: yyyy-MM-dd"));
        assertTrue(msg.contains("parameter 'dateOfBirth'"));
    }

    @Test
    void handleMethodArgumentTypeMismatchException_otherType_shouldUseSimpleName() {
        HttpServletRequest req = mockRequest("/api/mismatch2");

        MethodArgumentTypeMismatchException ex =
                new MethodArgumentTypeMismatchException(
                        "abc", Integer.class, "page", null, new IllegalArgumentException()
                );

        ResponseEntity<RestResponse<Void>> res =
                handler.handleMethodArgumentTypeMismatchException(ex, req);

        String msg = ((List<String>) res.getBody().getMessage()).get(0);
        assertTrue(msg.contains("Expected type: Integer"));
    }

    // ---------- generic Exception ----------
    @Test
    void handleException_shouldReturn403() {
        HttpServletRequest req = mockRequest("/api/forbidden");
        Exception ex = new Exception("forbidden stuff");

        ResponseEntity<RestResponse<Void>> res = handler.handleException(ex, req);

        assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
        assertEquals(403, res.getBody().getStatusCode());
        assertEquals(List.of("forbidden stuff"), res.getBody().getMessage());
    }

    // helper HttpInputMessage stub
    private HttpInputMessage dummyInputMessage() {
        return new HttpInputMessage() {
            @Override
            public InputStream getBody() {
                return new ByteArrayInputStream(new byte[0]);
            }
            @Override
            public HttpHeaders getHeaders() {
                return new HttpHeaders();
            }
        };
    }
}
