package com.genesyshub.infrastructure.web;

import com.genesyshub.domain.model.DomainException;
import com.genesyshub.infrastructure.web.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomainException(
            DomainException ex, HttpServletRequest request) {

        HttpStatus status = mapErrorCodeToStatus(ex.getCode());
        log.warn("Domain exception [{}]: {}", ex.getCode(), ex.getMessage());

        return ResponseEntity.status(status).body(new ErrorResponse(
                ex.getCode().name(),
                ex.getMessage(),
                Instant.now(),
                request.getRequestURI()
        ));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {

        log.debug("Constraint violation: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(new ErrorResponse(
                "VALIDATION_ERROR",
                ex.getMessage(),
                Instant.now(),
                request.getRequestURI()
        ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Validation failed");

        return ResponseEntity.badRequest().body(new ErrorResponse(
                "VALIDATION_ERROR",
                message,
                Instant.now(),
                request.getRequestURI()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex, HttpServletRequest request) {

        log.error("Unhandled exception on {}", request.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(
                "INTERNAL_ERROR",
                "An unexpected error occurred",
                Instant.now(),
                request.getRequestURI()
        ));
    }

    // -------------------------------------------------------------------------

    private HttpStatus mapErrorCodeToStatus(DomainException.ErrorCode code) {
        return switch (code) {
            case QUEUE_NOT_FOUND, AGENT_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case GENESYS_AUTH_FAILED, INVALID_WEBHOOK_SIGNATURE -> HttpStatus.UNAUTHORIZED;
            case RATE_LIMIT_EXCEEDED -> HttpStatus.TOO_MANY_REQUESTS;
            case GENESYS_API_ERROR -> HttpStatus.BAD_GATEWAY;
        };
    }
}
