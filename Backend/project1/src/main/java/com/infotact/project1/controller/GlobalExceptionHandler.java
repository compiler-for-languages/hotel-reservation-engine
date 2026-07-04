package com.infotact.project1.controller;

import com.infotact.project1.dto.response.ApiErrorResponse;
import com.infotact.project1.exception.ExceptionMessageResolver;
import com.infotact.project1.exception.ResourceNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

/*
 * Global exception handler.
 *
 * Converts application and infrastructure failures into structured,
 * user-friendly API responses. Technical details are logged for developers
 * but never returned to clients.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex) {

        log.warn("Resource not found: {}", ex.getMessage());
        return buildResponse(
                HttpStatus.NOT_FOUND,
                "Not Found",
                ExceptionMessageResolver.resolveRuntimeMessage(ex.getMessage())
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining(" "));

        if (message.isBlank()) {
            message = "Please check the submitted information and try again.";
        }

        log.warn("Validation failed: {}", message);
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex) {

        String message = ex.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .filter(value -> value != null && !value.isBlank())
                .collect(Collectors.joining(" "));

        if (message.isBlank()) {
            message = "Please check the submitted information and try again.";
        }

        log.warn("Constraint violation: {}", message);
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", message);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex) {

        log.warn("Illegal argument: {}", ex.getMessage(), ex);
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                ExceptionMessageResolver.resolveRuntimeMessage(ex.getMessage())
        );
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalStateException(
            IllegalStateException ex) {

        log.warn("Illegal state: {}", ex.getMessage(), ex);
        return buildResponse(
                HttpStatus.CONFLICT,
                "Conflict",
                ExceptionMessageResolver.resolveRuntimeMessage(ex.getMessage())
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentialsException(
            BadCredentialsException ex) {

        log.warn("Bad credentials: {}", ex.getMessage());
        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                "Unauthorized",
                "Invalid email or password."
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthenticationException(
            AuthenticationException ex) {

        log.warn("Authentication failed: {}", ex.getMessage());
        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                "Unauthorized",
                "Authentication failed. Please login again."
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex) {

        log.warn("Access denied: {}", ex.getMessage());
        return buildResponse(
                HttpStatus.FORBIDDEN,
                "Forbidden",
                "You are not authorized to perform this action."
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex) {

        log.error("Data integrity violation", ex);
        return buildResponse(
                HttpStatus.CONFLICT,
                "Conflict",
                ExceptionMessageResolver.resolveDataIntegrityMessage(ex)
        );
    }



    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorResponse> handleRuntimeException(
            RuntimeException ex) {

        String message = ex.getMessage();
        if (message != null && (
                "Invalid email or password".equals(message)
                        || "Invalid email or password.".equals(message))) {
            log.warn("Invalid login credentials");
            return buildResponse(
                    HttpStatus.UNAUTHORIZED,
                    "Unauthorized",
                    "Invalid email or password."
            );
        }

        String safeMessage = ExceptionMessageResolver.resolveRuntimeMessage(message);

        if (ExceptionMessageResolver.GENERIC_MESSAGE.equals(safeMessage)) {
            log.error("Unhandled runtime exception", ex);
        } else {
            log.warn("Business runtime exception: {}", ex.getMessage());
        }

        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", safeMessage);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(Exception ex) {

        log.error("Unexpected exception", ex);
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                ExceptionMessageResolver.GENERIC_MESSAGE
        );
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            String error,
            String message) {

        ApiErrorResponse body = ApiErrorResponse.builder()
                .timestamp(Instant.now().toString())
                .status(status.value())
                .error(error)
                .message(message)
                .build();

        return ResponseEntity.status(status).body(body);
    }

    private String formatFieldError(FieldError fieldError) {
        String message = fieldError.getDefaultMessage();
        if (message != null && !message.isBlank()) {
            return message.endsWith(".") ? message : message + ".";
        }
        return "Invalid value for " + fieldError.getField() + ".";
    }
}
