package com.saas.billing.auth_service.service;


import com.saas.billing.auth_service.domain.entity.RefreshToken;
import com.saas.billing.auth_service.domain.entity.User;
import com.saas.billing.auth_service.domain.enums.Role;
import com.saas.billing.auth_service.dto.request.LoginRequest;
import com.saas.billing.auth_service.dto.request.RegisterRequest;
import com.saas.billing.auth_service.dto.response.AuthResponse;
import com.saas.billing.auth_service.dto.response.UserResponse;
import com.saas.billing.auth_service.exception.AccountBlockedException;
import com.saas.billing.auth_service.exception.EmailAlreadyExistsException;
import com.saas.billing.auth_service.exception.InvalidCredentialsException;
import com.saas.billing.auth_service.exception.UserNotFoundException;
import com.saas.billing.auth_service.messaging.producer.UserEventPublisher;
import com.saas.billing.auth_service.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final BruteForceProtectionService bruteForceProtectionService;
    private final PasswordEncoder passwordEncoder;
    private final UserEventPublisher userEventPublisher;

    public AuthService(UserRepository userRepository, JwtService jwtService, RefreshTokenService refreshTokenService, BruteForceProtectionService bruteForceProtectionService, PasswordEncoder passwordEncoder, UserEventPublisher userEventPublisher) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.bruteForceProtectionService = bruteForceProtectionService;
        this.passwordEncoder = passwordEncoder;
        this.userEventPublisher = userEventPublisher;
    }

    // ════════════════════════════════════
    // INSCRIPTION
    // ════════════════════════════════════
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Verify if email already in use
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(
                    "Email déjà utilisé : " + request.email()
            );
        }

        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(Role.CUSTOMER)
                .build();

        user = userRepository.save(user);

        // publish event UserCreated in Kafka
        // → subscription-service va stocker ce user and other subscribed services
        //   dans sa table users_cache

        userEventPublisher.publishUserCreated(user);

        // Generate tokens
        String accessToken = jwtService.generateToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user);

        // missing email verification otp
        return buildAuthResponse(user, accessToken, refreshToken);

    }

    // ════════════════════════════════════
    // Login
    // ════════════════════════════════════
    @Transactional
    public AuthResponse login(LoginRequest request) {
        // Verify if account is blocked
        if (bruteForceProtectionService.isBlocked(request.email())){
            long minutesRemaining = bruteForceProtectionService
                    .getBlockedMinutesRemaining(request.email());

            throw new AccountBlockedException(
                    "Account blocked. Try in " + minutesRemaining + " minutes."
            );
        }

        // find the user
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    // save attempt is failed even if email is unknown
                    bruteForceProtectionService
                            .registerFailedAttempt(request.email());
                    return new InvalidCredentialsException(
                            "Invalid email or password"
                    );
                });

        // Verify password
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            bruteForceProtectionService.registerFailedAttempt(request.email());

            // if this attempts blocks the account
            if (bruteForceProtectionService.isBlocked(request.email())) {
                long minutesRemaining = bruteForceProtectionService
                        .getBlockedMinutesRemaining(request.email());

                throw new AccountBlockedException(
                        "Account blocked. Try again in " + minutesRemaining + " minutes."
                );
            }

            // else account not blocked yet
            int remaining = bruteForceProtectionService
                    .getRemainingAttempts(request.email());

            throw new InvalidCredentialsException(
                    "Invalid email or password. " +
                            remaining + " attempts remaining."
            );

        }

        // Login success -> resetAttempts
        bruteForceProtectionService.resetAttempts(request.email());

        // Update last login
        userRepository.updateLastLoginAt(user.getId(), LocalDateTime.now());

        // Generate tokens
        String accessToken = jwtService.generateToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user);

        return buildAuthResponse(user, accessToken, refreshToken);

    }

    // ════════════════════════════════════
    // REFRESH TOKEN - renew jwt token
    // ════════════════════════════════════

    @Transactional
    public AuthResponse refreshToken(String rawRefreshToken) {

        // validate refresh token
        RefreshToken refreshToken = refreshTokenService
                .validateRefreshToken(rawRefreshToken);

        User user = refreshToken.getUser();

        // Generate new access token (jwt)
        String newAccessToken = jwtService.generateToken(user);

        // generate new refresh token
        String newRefreshToken = refreshTokenService.createRefreshToken(user);

        return buildAuthResponse(user, newAccessToken, newRefreshToken);
    }

    // ════════════════════════════════════
    // LOGOUT
    // ════════════════════════════════════

    @Transactional
    public void logout(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found"
                ));

        // delete refresh token
        refreshTokenService.deleteRefreshToken(user);

        // maybe deleting tokens from client
    }

    // ════════════════════════════════════
    // GET USER
    // ════════════════════════════════════

    @Transactional(readOnly = true)
    public UserResponse getUserById(String userId) {
        User user = userRepository.findById(
                java.util.UUID.fromString(userId)
        ).orElseThrow(() -> new RuntimeException("User not found"));

        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .build();
    }

    // ════════════════════════════════════
    // Private Method
    // ════════════════════════════════════
    private AuthResponse buildAuthResponse(
            User user,
            String accessToken,
            String refreshToken) {

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpiration() / 1000)
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .build();
    }
}
