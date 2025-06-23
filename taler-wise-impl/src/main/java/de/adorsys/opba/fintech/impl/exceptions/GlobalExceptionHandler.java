//package de.adorsys.opba.fintech.impl.exceptions;
//
//import de.adorsys.opba.fintech.impl.service.exceptions.InvalidIbanException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//
//@RestControllerAdvice
//public class GlobalExceptionHandler {
//
//    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);
//
//    @ExceptionHandler(InvalidIbanException.class)
//    public ResponseEntity<String> handleInvalidIban(InvalidIbanException ex) {
//        LOG.warn("Invalid IBAN request: {}", ex.getMessage());
//        return ResponseEntity.badRequest().body(ex.getMessage());
//    }
//
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<String> handleGenericException(Exception ex) {
//        LOG.error("Unexpected error: {}", ex.getMessage());
//        return ResponseEntity.internalServerError().body("An unexpected error occurred. Please try again later.");
//    }
//}

package de.adorsys.opba.fintech.impl.exceptions;

import de.adorsys.opba.fintech.impl.service.exceptions.InvalidIbanException;
import org.iban4j.InvalidCheckDigitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        LOG.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.internalServerError().body("An unexpected error occurred. Please try again later.");
    }
}
