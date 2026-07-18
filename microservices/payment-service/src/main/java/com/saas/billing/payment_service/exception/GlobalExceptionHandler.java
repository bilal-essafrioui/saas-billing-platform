package com.saas.billing.payment_service.exception;

import com.saas.billing.payment_service.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ════════════════════════════════════
    // PAYMENT NOT FOUND → 404
    // ════════════════════════════════════

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePaymentNotFound(
            PaymentNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(
                        404,
                        "PAYMENT_NOT_FOUND",
                        ex.getMessage()
                ));
    }

    // ════════════════════════════════════
    // PAYMENT METHOD NOT FOUND → 404
    // ════════════════════════════════════

    @ExceptionHandler(PaymentMethodNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotFound(
            PaymentMethodNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(
                        404,
                        "PAYMENT_METHOD_NOT_FOUND",
                        ex.getMessage()
                ));
    }

    // ════════════════════════════════════
    // ALREADY PROCESSED → 409
    // paiement déjà traité (idempotence)
    // ════════════════════════════════════

    @ExceptionHandler(PaymentAlreadyProcessedException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyProcessed(
            PaymentAlreadyProcessedException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(
                        409,
                        "PAYMENT_ALREADY_PROCESSED",
                        ex.getMessage()
                ));
    }

    // ════════════════════════════════════
    // INVALID WEBHOOK SIGNATURE → 400
    // tentative de fausse requête Stripe
    // ════════════════════════════════════

    @ExceptionHandler(InvalidWebhookSignatureException.class)
    public ResponseEntity<ErrorResponse> handleInvalidSignature(
            InvalidWebhookSignatureException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(
                        400,
                        "INVALID_WEBHOOK_SIGNATURE",
                        ex.getMessage()
                ));
    }

    // ════════════════════════════════════
    // STRIPE ERROR → 502
    // erreur de communication avec Stripe
    // ════════════════════════════════════

    @ExceptionHandler(StripePaymentException.class)
    public ResponseEntity<ErrorResponse> handleStripeError(
            StripePaymentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(ErrorResponse.of(
                        502,
                        "STRIPE_ERROR",
                        ex.getMessage()
                ));
    }

    // ════════════════════════════════════
    // VALIDATION → 400
    // ════════════════════════════════════

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error -> errors.put(
                        error.getField(),
                        error.getDefaultMessage()
                ));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errors);
    }

    // ════════════════════════════════════
    // GENERIC → 500
    // ════════════════════════════════════

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(
                        500,
                        "INTERNAL_SERVER_ERROR",
                        "An unexpected error occurred"
                ));
    }
}