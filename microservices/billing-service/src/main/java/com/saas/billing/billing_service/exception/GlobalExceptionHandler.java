package com.saas.billing.billing_service.exception;

import com.saas.billing.billing_service.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvoiceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            InvoiceNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(404,
                        "INVOICE_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(InvoiceAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyExists(
            InvoiceAlreadyExistsException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(409,
                        "INVOICE_ALREADY_EXISTS", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(500,
                        "INTERNAL_SERVER_ERROR",
                        "An unexpected error occurred"));
    }
}