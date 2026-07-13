package com.saas.billing.subscription_service.exception;

import com.saas.billing.subscription_service.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ════════════════════════════════════
    // PLAN NOT FOUND → 404
    // ════════════════════════════════════

    @ExceptionHandler(PlanNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePlanNotFound(
            PlanNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(404, "PLAN_NOT_FOUND",
                        ex.getMessage()));
    }

    // ════════════════════════════════════
    // SUBSCRIPTION NOT FOUND → 404
    // ════════════════════════════════════

    @ExceptionHandler(SubscriptionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSubscriptionNotFound(
            SubscriptionNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(404, "SUBSCRIPTION_NOT_FOUND",
                        ex.getMessage()));
    }

    // ════════════════════════════════════
    // SUBSCRIPTION ALREADY EXISTS → 409
    // ════════════════════════════════════

    @ExceptionHandler(SubscriptionAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyExists(
            SubscriptionAlreadyExistsException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(409, "SUBSCRIPTION_ALREADY_EXISTS",
                        ex.getMessage()));
    }

    // ════════════════════════════════════
    // INVALID STATE TRANSITION → 422
    // ════════════════════════════════════

    @ExceptionHandler(InvalidStateTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTransition(
            InvalidStateTransitionException ex) {
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ErrorResponse.of(422, "INVALID_STATE_TRANSITION",
                        ex.getMessage()));
    }

    // ════════════════════════════════════
    // USER NOT FOUND → 404
    // ════════════════════════════════════

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(
            UserNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(404, "USER_NOT_FOUND",
                        ex.getMessage()));
    }

    // ════════════════════════════════════
    // ILLEGAL ARGUMENT → 400
    // ex: même plan sélectionné
    // ════════════════════════════════════

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(400, "BAD_REQUEST",
                        ex.getMessage()));
    }

    // ════════════════════════════════════
    // VALIDATION → 400
    // ════════════════════════════════════

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, List<String>>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, List<String>> errors = new HashMap<>();

        ex.getBindingResult()
                .getAllErrors()
                .forEach(error -> {
                    String fieldName = ((FieldError) error).getField();
                    String errorMessage = error.getDefaultMessage();
                    errors
                            .computeIfAbsent(fieldName, key -> new ArrayList<>())
                            .add(errorMessage);
                });

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errors);
    }

    // ════════════════════════════════════
    // GENERIC → 500
    // ════════════════════════════════════

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(500, "INTERNAL_SERVER_ERROR",
                        "An unexpected error occurred"));
    }
}