package com.example.test_order_service.exception;

import com.example.test_order_service.dto.response.RestResponse;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // Helper method to get custom error message for date fields
    // Add more fields if needed
    private String getDateErrorMessage(String fieldName) {
        return switch (fieldName) {
            case "dateOfBirth" -> "Birthdate must in dd/MM/yyyy format";
            default -> "Date must in dd/MM/yyyy format";
        };
    }

    private String getEnumErrorMessage(String fieldName, Class<?> enumType) {
        // Get all enum values dynamically
        Object[] enumConstants = enumType.getEnumConstants();

        // Convert to readable format: "MALE, FEMALE, OTHER"
        String validValues = Arrays.stream(enumConstants)
                .map(Object::toString)
                .collect(Collectors.joining(", "));

        // Capitalize first letter of field name for better readability
        String fieldDisplayName = fieldName.substring(0, 1).toUpperCase() +
                fieldName.substring(1);

        return fieldDisplayName + " must be one of: " + validValues;
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<RestResponse> handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
        RestResponse errorResponse = RestResponse.builder()
                .timestamp(LocalDateTime.now())
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RestResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> errorMessages = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());

        RestResponse errorResponse = RestResponse.builder()
                .timestamp(java.time.LocalDateTime.now())
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(errorMessages)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<RestResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        List<String> errorMessages = new ArrayList<>();

        // Handle the error in parsing JSON
        if (ex.getCause() instanceof InvalidFormatException) {
            InvalidFormatException ifx = (InvalidFormatException) ex.getCause();

            // Check if error for parsing LocalDate
            if (ifx.getTargetType() != null && ifx.getTargetType().equals(LocalDate.class)) {
                String fieldName = ifx.getPath().get(0).getFieldName();
                String message = getDateErrorMessage(fieldName);
                errorMessages.add(message);
            }
            // Check if error for parsing Enum
            else if (ifx.getTargetType() != null && ifx.getTargetType().isEnum()) {
                String fieldName = ifx.getPath().isEmpty() ? "unknown" : ifx.getPath().get(0).getFieldName();
                String message = getEnumErrorMessage(fieldName, ifx.getTargetType());
                errorMessages.add(message);
            }
        }
        // JSON syntax error or other errors
        else {
            errorMessages.add("Invalid JSON format. Please check the data structure.");
        }

        RestResponse errorResponse = RestResponse.builder()
                .timestamp(LocalDateTime.now())
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(errorMessages)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<RestResponse<Void>> handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest request) {
        RestResponse<Void> response = RestResponse.<Void>builder()
                .timestamp(LocalDateTime.now())
                .statusCode(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message(List.of(ex.getMessage()))
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<RestResponse<Void>> handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
        RestResponse<Void> response = RestResponse.<Void>builder()
                .timestamp(LocalDateTime.now())
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(List.of(ex.getMessage()))
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<RestResponse<Void>> handleIllegalStateException(IllegalStateException ex, HttpServletRequest request) {
        RestResponse<Void> response = RestResponse.<Void>builder()
                .timestamp(LocalDateTime.now())
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(List.of(ex.getMessage()))
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public  ResponseEntity<RestResponse<Void>> handleException(Exception ex, HttpServletRequest request) {
        RestResponse<Void> response = RestResponse.<Void>builder()
                .timestamp(LocalDateTime.now())
                .statusCode(HttpStatus.FORBIDDEN.value())
                .error(HttpStatus.FORBIDDEN.getReasonPhrase())
                .message(List.of(ex.getMessage()))
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }
}
