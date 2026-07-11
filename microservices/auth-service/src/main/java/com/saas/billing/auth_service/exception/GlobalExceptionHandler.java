package com.saas.billing.auth_service.exception;

import com.saas.billing.auth_service.dto.response.ErrorResponse;
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
    // EMAIL ALREADY EXISTS → 409 CONFLICT
    // ════════════════════════════════════

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExists(
            EmailAlreadyExistsException ex) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(
                        409,
                        "EMAIL_ALREADY_EXISTS",
                        ex.getMessage()
                ));
    }

    // ════════════════════════════════════
    // BAD CREDENTIALS → 401 UNAUTHORIZED
    // ════════════════════════════════════

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(
            InvalidCredentialsException ex) {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.of(
                        401,
                        "INVALID_CREDENTIALS",
                        ex.getMessage()
                ));
    }

    // ════════════════════════════════════
    // ACCOUNT BLOCKED → 423 LOCKED
    // ════════════════════════════════════

    @ExceptionHandler(AccountBlockedException.class)
    public ResponseEntity<ErrorResponse> handleAccountBlocked(
            AccountBlockedException ex) {

        return ResponseEntity
                .status(HttpStatus.LOCKED)
                .body(ErrorResponse.of(
                        423,
                        "ACCOUNT_LOCKED",
                        ex.getMessage()
                ));
    }

    // ════════════════════════════════════
    // TOKEN EXPIRED → 401 UNAUTHORIZED
    // ════════════════════════════════════

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ErrorResponse> handleTokenExpired(
            TokenExpiredException ex) {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.of(
                        401,
                        "TOKEN_EXPIRED",
                        ex.getMessage()
                ));
    }

    // ════════════════════════════════════
    // TOKEN INVALID → 401 UNAUTHORIZED
    // ════════════════════════════════════

    @ExceptionHandler(TokenInvalidException.class)
    public ResponseEntity<ErrorResponse> handleTokenInvalid(
            TokenInvalidException ex) {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.of(
                        401,
                        "TOKEN_INVALID",
                        ex.getMessage()
                ));
    }

    // ════════════════════════════════════
    // USER NOT FOUND → 404 NOT FOUND
    // ════════════════════════════════════

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(
            UserNotFoundException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(
                        404,
                        "USER_NOT_FOUND",
                        ex.getMessage()
                ));
    }

    // ════════════════════════════════════
    // INPUTS VALIDATION → 400 BAD REQUEST
    // called when @Valid failed sur un DTO
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
    // GENERAL ERRORS → 500
    // ════════════════════════════════════

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex) {

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(
                        500,
                        "INTERNAL_SERVER_ERROR",
                        "An unexpected error occurred."
                ));
    }
}
