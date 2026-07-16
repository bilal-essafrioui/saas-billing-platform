package com.saas.billing.auth_service.controller;

import com.saas.billing.auth_service.dto.request.LoginRequest;
import com.saas.billing.auth_service.dto.request.RefreshTokenRequest;
import com.saas.billing.auth_service.dto.request.RegisterRequest;
import com.saas.billing.auth_service.dto.request.VerifyEmailRequest;
import com.saas.billing.auth_service.dto.response.AuthResponse;
import com.saas.billing.auth_service.dto.response.RegistrationPendingResponse;
import com.saas.billing.auth_service.dto.response.UserResponse;
import com.saas.billing.auth_service.security.AuthenticatedUser;
import com.saas.billing.auth_service.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // ════════════════════════════════════
    // POST /api/auth/register
    // public endpoint
    // ════════════════════════════════════

    @PostMapping("/register")
    public ResponseEntity<RegistrationPendingResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        RegistrationPendingResponse response = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // ════════════════════════════════════
    // POST /api/auth/verify-email
    // public endpoint
    // ════════════════════════════════════
    @PostMapping("/verify-email")
    public ResponseEntity<AuthResponse> verifyEmail(
            @Valid @RequestBody VerifyEmailRequest request
    ) {
        return ResponseEntity.ok(authService.verifyEmail(request));
    }

    // ════════════════════════════════════
    // POST /api/auth/login
    // public endpoint
    // ════════════════════════════════════

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    // ════════════════════════════════════
    // POST /api/auth/refresh
    // public endpoint
    // Gateway extract refresh_token from cookie
    // and send it in the body
    // ════════════════════════════════════

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {

        AuthResponse response = authService.refreshToken(
                request.refreshToken()
        );
        return ResponseEntity.ok(response);
    }

    // ════════════════════════════════════
    // POST /api/auth/logout
    // protected endpoint
    // Gateway send Authorization: Bearer {jwt}
    // JwtAuthFilter extract email + userId from JWT
    // and put them in SecurityContext
    // ════════════════════════════════════

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication) {

        // Get the authenticated user from Spring Security
        // userId + email
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();

        authService.logout(user.email());

        return ResponseEntity.noContent().build();
    }

    // ════════════════════════════════════
    // GET /api/auth/me
    // protected endpoint
    // Gateway send Authorization: Bearer {jwt}
    // JwtAuthFilter extract userId and email du JWT
    // and put them in security context
    // ════════════════════════════════════

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {

        // Get the authenticated user from Spring Security
        // userId and email
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();

        UserResponse response = authService.getUserById(user.userId());

        return ResponseEntity.ok(response);
    }

    // ════════════════════════════════════
    // GET /api/auth/validate
    // public endpoint (called by Gateway)
    // Gateway send Authorization: Bearer {jwt}
    // JwtAuthFilter validate le token
    // if we are here  → token valid → 200 OK
    // ════════════════════════════════════

    /*@GetMapping("/validate")
    public ResponseEntity<Void> validateToken() {
        return ResponseEntity.ok().build();
    }*/
}