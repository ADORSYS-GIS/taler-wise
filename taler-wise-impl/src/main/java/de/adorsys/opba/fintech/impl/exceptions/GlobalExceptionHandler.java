package de.adorsys.opba.fintech.impl.exceptions;

import org.iban4j.InvalidCheckDigitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InvalidIbanException.class)
    public ResponseEntity<String> handleInvalidIban(InvalidIbanException ex) {
        LOG.warn("Invalid IBAN request: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(InvalidCheckDigitException.class)
    public ResponseEntity<String> handleInvalidCheckDigit(InvalidCheckDigitException ex) {
        LOG.warn("Invalid IBAN check digit: {}", ex.getMessage());
        return ResponseEntity.badRequest().body("Invalid IBAN: " + ex.getMessage());
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<String> handleMissingHeader(MissingRequestHeaderException ex) {
        LOG.warn("Missing required header: {}", ex.getHeaderName());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body("Missing header: " + ex.getHeaderName());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidation(MethodArgumentNotValidException ex) {
        String errorMsg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .findFirst().orElse("Validation error");
        LOG.warn("Validation failed: {}", errorMsg);
        return ResponseEntity.badRequest().body(errorMsg);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> handleResponseStatusException(
        ResponseStatusException ex
    ) {
        String reason = ex.getReason();
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());

        // Log appropriately based on status code
        if (status.is4xxClientError()) {
            LOG.warn("Client error ({}): {}", status, reason);
        } else {
            LOG.error("Response status exception ({}): {}", status, reason);
        }

        return ResponseEntity.status(status).body(reason);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        LOG.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.internalServerError().body("An unexpected error occurred. Please try again later.");
    }
}
