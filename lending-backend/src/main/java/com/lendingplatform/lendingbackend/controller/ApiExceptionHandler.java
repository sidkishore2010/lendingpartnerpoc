package com.lendingplatform.lendingbackend.controller;

import com.lendingplatform.lendingbackend.dto.ErrorResponse;
import com.lendingplatform.lendingbackend.service.CommissionApiException;
import com.lendingplatform.lendingbackend.service.VendorIntegrationException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.stream.Collectors;

@ControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        String correlationId = getCorrelationId(request);
        log.warn("Validation failed [correlationId={}]: {}", correlationId, message);
        return ResponseEntity.badRequest().body(new ErrorResponse("VALIDATION_ERROR", message, correlationId, Instant.now()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMalformedJson(HttpMessageNotReadableException ex, HttpServletRequest request) {
        String correlationId = getCorrelationId(request);
        log.warn("Malformed JSON [correlationId={}]: {}", correlationId, ex.getMessage());
        return ResponseEntity.badRequest().body(new ErrorResponse("MALFORMED_JSON", "Request body is malformed or missing.", correlationId, Instant.now()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        String correlationId = getCorrelationId(request);
        log.warn("Illegal argument [correlationId={}]: {}", correlationId, ex.getMessage());
        return ResponseEntity.badRequest().body(new ErrorResponse("INVALID_INPUT", ex.getMessage(), correlationId, Instant.now()));
    }

    @ExceptionHandler(VendorIntegrationException.class)
    public ResponseEntity<ErrorResponse> handleVendorIntegration(VendorIntegrationException ex, HttpServletRequest request) {
        String correlationId = getCorrelationId(request);
        log.error("Vendor integration error [correlationId={}][code={}]: {}", correlationId, ex.getCode(), ex.getMessage());
        return ResponseEntity.status(ex.getStatus())
                .body(new ErrorResponse(ex.getCode(), ex.getMessage(), correlationId, Instant.now()));
    }

    @ExceptionHandler(CommissionApiException.class)
    public ResponseEntity<ErrorResponse> handleCommissionApiException(CommissionApiException ex, HttpServletRequest request) {
        String correlationId = getCorrelationId(request);
        log.warn("Commission API error [correlationId={}][code={}]: {}", correlationId, ex.getCode(), ex.getMessage());
        return ResponseEntity.status(ex.getStatus())
                .body(new ErrorResponse(ex.getCode(), ex.getMessage(), correlationId, Instant.now()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception ex, HttpServletRequest request) {
        String correlationId = getCorrelationId(request);
        log.error("Unexpected exception [correlationId={}]", correlationId, ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred while generating the quote.", correlationId, Instant.now()));
    }

    private String getCorrelationId(HttpServletRequest request) {
        String correlationId = (String) request.getAttribute("correlationId");
        return correlationId != null ? correlationId : MDC.get("correlationId");
    }
}
