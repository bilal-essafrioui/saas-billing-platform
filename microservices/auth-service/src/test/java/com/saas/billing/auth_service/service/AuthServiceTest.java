package com.saas.billing.auth_service.service;


import com.saas.billing.auth_service.domain.entity.User;
import com.saas.billing.auth_service.domain.enums.Role;
import com.saas.billing.auth_service.dto.request.LoginRequest;
import com.saas.billing.auth_service.dto.request.PendingRegistrationRequest;
import com.saas.billing.auth_service.dto.request.RegisterRequest;
import com.saas.billing.auth_service.dto.request.VerifyEmailRequest;
import com.saas.billing.auth_service.dto.response.AuthResponse;
import com.saas.billing.auth_service.dto.response.RegistrationPendingResponse;
import com.saas.billing.auth_service.exception.*;
import com.saas.billing.auth_service.messaging.producer.UserEventPublisher;
import com.saas.billing.auth_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private BruteForceProtectionService bruteForceProtectionService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserEventPublisher userEventPublisher;

    @Mock
    private PendingRegistrationService pendingRegistrationService;

    @Mock
    private OtpService otpService;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private PendingRegistrationRequest testPendingRegistration;
    private VerifyEmailRequest verifyEmailRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .firstName("Ahmed")
                .lastName("Benali")
                .email("ahmed@email.com")
                .passwordHash("$2a$12$hashedPassword")
                .role(Role.CUSTOMER)
                .build();

        // RegisterRequest / LoginRequest are records -> use the canonical
        // constructor, there is no .builder() on them.
        registerRequest = new RegisterRequest(
                "Ahmed",
                "Benali",
                "ahmed@email.com",
                "Password123!"
        );

        loginRequest = new LoginRequest("ahmed@email.com", "Password123!");

        testPendingRegistration = PendingRegistrationRequest.builder()
                .firstName("Ahmed")
                .lastName("Benali")
                .email("ahmed@email.com")
                .passwordHash("$2a$12$hashedPassword")
                .build();

        verifyEmailRequest = new VerifyEmailRequest("ahmed@email.com", "123456");
    }

    // ════════════════════════════════════
    // REGISTER TESTS (now only creates a pending registration + sends OTP)
    // ════════════════════════════════════

    @Nested
    @DisplayName("Register")
    class RegisterTests {

        @Test
        @DisplayName("Register réussi → pending registration créée + OTP envoyé")
        void register_success_shouldCreatePendingRegistrationAndSendOtp() {
            // GIVEN
            when(userRepository.existsByEmail(anyString()))
                    .thenReturn(false);
            when(pendingRegistrationService.exists(anyString()))
                    .thenReturn(false);
            when(passwordEncoder.encode(anyString()))
                    .thenReturn("$2a$12$hashedPassword");
            when(otpService.generateOtp())
                    .thenReturn("123456");

            // WHEN
            RegistrationPendingResponse result =
                    authService.register(registerRequest);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.email()).isEqualTo("ahmed@email.com");

            verify(pendingRegistrationService)
                    .save(any(PendingRegistrationRequest.class));
            verify(otpService).saveOtp(eq("ahmed@email.com"), eq("123456"));
            verify(userEventPublisher)
                    .publishOtpGenerated("ahmed@email.com", "123456");

            // no user / login tokens should be created at this stage
            verify(userRepository, never()).save(any());
            verify(userEventPublisher, never()).publishUserCreated(any());
        }

        @Test
        @DisplayName("Register avec email existant → EmailAlreadyExistsException")
        void register_emailExists_shouldThrow() {
            // GIVEN
            when(userRepository.existsByEmail("ahmed@email.com"))
                    .thenReturn(true);

            // WHEN + THEN
            assertThatThrownBy(() ->
                    authService.register(registerRequest)
            )
                    .isInstanceOf(EmailAlreadyExistsException.class)
                    .hasMessageContaining("ahmed@email.com");

            verify(pendingRegistrationService, never()).save(any());
            verify(userEventPublisher, never())
                    .publishOtpGenerated(anyString(), anyString());
        }

        @Test
        @DisplayName("Register avec vérification déjà en cours → PendingRegistrationAlreadyExistsException")
        void register_pendingRegistrationAlreadyExists_shouldThrow() {
            // GIVEN
            when(userRepository.existsByEmail(anyString()))
                    .thenReturn(false);
            when(pendingRegistrationService.exists("ahmed@email.com"))
                    .thenReturn(true);

            // WHEN + THEN
            assertThatThrownBy(() ->
                    authService.register(registerRequest)
            )
                    .isInstanceOf(PendingRegistrationAlreadyExistsException.class);

            verify(pendingRegistrationService, never()).save(any());
            verify(otpService, never()).generateOtp();
        }

        @Test
        @DisplayName("Register → mot de passe hashé avec BCrypt")
        void register_shouldHashPassword() {
            // GIVEN
            when(userRepository.existsByEmail(anyString()))
                    .thenReturn(false);
            when(pendingRegistrationService.exists(anyString()))
                    .thenReturn(false);
            when(passwordEncoder.encode("Password123!"))
                    .thenReturn("$2a$12$hashedPassword");
            when(otpService.generateOtp())
                    .thenReturn("123456");

            // WHEN
            authService.register(registerRequest);

            // THEN
            verify(passwordEncoder).encode("Password123!");
            verify(pendingRegistrationService).save(
                    argThat(p -> p.passwordHash().equals("$2a$12$hashedPassword"))
            );
        }
    }

    // ════════════════════════════════════
    // VERIFY EMAIL TESTS (this is where the user is actually created
    // and tokens are issued)
    // ════════════════════════════════════

    @Nested
    @DisplayName("VerifyEmail")
    class VerifyEmailTests {

        @Test
        @DisplayName("Vérification réussie → user créé + tokens retournés")
        void verifyEmail_success_shouldCreateUserAndReturnTokens() {
            // GIVEN
            when(pendingRegistrationService.get("ahmed@email.com"))
                    .thenReturn(testPendingRegistration);
            when(otpService.exists("ahmed@email.com"))
                    .thenReturn(true);
            when(otpService.verifyOtp("ahmed@email.com", "123456"))
                    .thenReturn(true);
            when(userRepository.existsByEmail("ahmed@email.com"))
                    .thenReturn(false);
            when(userRepository.save(any(User.class)))
                    .thenReturn(testUser);
            when(jwtService.generateToken(any(User.class)))
                    .thenReturn("jwt_token_mock");
            when(refreshTokenService.createRefreshToken(any(User.class)))
                    .thenReturn("refresh_token_mock");

            // WHEN
            AuthResponse result = authService.verifyEmail(verifyEmailRequest);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.accessToken()).isEqualTo("jwt_token_mock");
            assertThat(result.refreshToken()).isEqualTo("refresh_token_mock");
            assertThat(result.email()).isEqualTo("ahmed@email.com");
            assertThat(result.role()).isEqualTo(Role.CUSTOMER);

            verify(userRepository).save(any(User.class));
            verify(userEventPublisher).publishUserCreated(any());
            verify(otpService).deleteOtp("ahmed@email.com");
            verify(pendingRegistrationService).delete("ahmed@email.com");
        }

        @Test
        @DisplayName("Pending registration introuvable → PendingRegistrationNotFoundException")
        void verifyEmail_pendingRegistrationNotFound_shouldThrow() {
            // GIVEN
            when(pendingRegistrationService.get("ahmed@email.com"))
                    .thenReturn(null);

            // WHEN + THEN
            assertThatThrownBy(() ->
                    authService.verifyEmail(verifyEmailRequest)
            )
                    .isInstanceOf(PendingRegistrationNotFoundException.class);

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("OTP expiré → InvalidOtpException")
        void verifyEmail_otpExpired_shouldThrow() {
            // GIVEN
            when(pendingRegistrationService.get("ahmed@email.com"))
                    .thenReturn(testPendingRegistration);
            when(otpService.exists("ahmed@email.com"))
                    .thenReturn(false);

            // WHEN + THEN
            assertThatThrownBy(() ->
                    authService.verifyEmail(verifyEmailRequest)
            )
                    .isInstanceOf(InvalidOtpException.class)
                    .hasMessageContaining("expired");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("OTP invalide → InvalidOtpException")
        void verifyEmail_otpInvalid_shouldThrow() {
            // GIVEN
            when(pendingRegistrationService.get("ahmed@email.com"))
                    .thenReturn(testPendingRegistration);
            when(otpService.exists("ahmed@email.com"))
                    .thenReturn(true);
            when(otpService.verifyOtp("ahmed@email.com", "123456"))
                    .thenReturn(false);

            // WHEN + THEN
            assertThatThrownBy(() ->
                    authService.verifyEmail(verifyEmailRequest)
            )
                    .isInstanceOf(InvalidOtpException.class)
                    .hasMessageContaining("Invalid");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Email déjà pris entre-temps → EmailAlreadyExistsException")
        void verifyEmail_emailAlreadyExists_shouldThrow() {
            // GIVEN — simulates the final safety check catching a race condition
            when(pendingRegistrationService.get("ahmed@email.com"))
                    .thenReturn(testPendingRegistration);
            when(otpService.exists("ahmed@email.com"))
                    .thenReturn(true);
            when(otpService.verifyOtp("ahmed@email.com", "123456"))
                    .thenReturn(true);
            when(userRepository.existsByEmail("ahmed@email.com"))
                    .thenReturn(true);

            // WHEN + THEN
            assertThatThrownBy(() ->
                    authService.verifyEmail(verifyEmailRequest)
            )
                    .isInstanceOf(EmailAlreadyExistsException.class);

            verify(userRepository, never()).save(any());
        }
    }

    // ════════════════════════════════════
    // LOGIN TESTS
    // ════════════════════════════════════

    @Nested
    @DisplayName("Login")
    class LoginTests {

        @Test
        @DisplayName("Login réussi → tokens retournés")
        void login_success_shouldReturnTokens() {
            // GIVEN
            when(bruteForceProtectionService
                    .isBlocked("ahmed@email.com"))
                    .thenReturn(false);
            when(userRepository.findByEmail("ahmed@email.com"))
                    .thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(
                    "Password123!",
                    testUser.getPasswordHash()))
                    .thenReturn(true);
            when(jwtService.generateToken(testUser))
                    .thenReturn("jwt_token");
            when(refreshTokenService.createRefreshToken(testUser))
                    .thenReturn("refresh_token");

            // WHEN
            AuthResponse result = authService.login(loginRequest);

            // THEN
            assertThat(result.accessToken()).isEqualTo("jwt_token");
            assertThat(result.refreshToken()).isEqualTo("refresh_token");
            assertThat(result.email()).isEqualTo("ahmed@email.com");
            assertThat(result.role()).isEqualTo(Role.CUSTOMER);

            // compteur d'échecs reset après succès
            verify(bruteForceProtectionService)
                    .resetAttempts("ahmed@email.com");

            // last login updated
            verify(userRepository)
                    .updateLastLoginAt(eq(testUser.getId()), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("Login compte bloqué → AccountBlockedException")
        void login_accountBlocked_shouldThrow() {
            // GIVEN
            when(bruteForceProtectionService
                    .isBlocked("ahmed@email.com"))
                    .thenReturn(true);
            when(bruteForceProtectionService
                    .getBlockedMinutesRemaining("ahmed@email.com"))
                    .thenReturn(12L);

            // WHEN + THEN
            assertThatThrownBy(() ->
                    authService.login(loginRequest)
            )
                    .isInstanceOf(AccountBlockedException.class)
                    .hasMessageContaining("12");

            // vérifier que le user n'est pas cherché en DB
            verify(userRepository, never())
                    .findByEmail(anyString());
        }

        @Test
        @DisplayName("Login email inconnu → InvalidCredentialsException")
        void login_unknownEmail_shouldThrow() {
            // GIVEN
            when(bruteForceProtectionService
                    .isBlocked(anyString()))
                    .thenReturn(false);
            when(userRepository.findByEmail("ahmed@email.com"))
                    .thenReturn(Optional.empty());

            // WHEN + THEN
            assertThatThrownBy(() ->
                    authService.login(loginRequest)
            )
                    .isInstanceOf(InvalidCredentialsException.class)
                    // message volontairement vague pour sécurité
                    .hasMessageContaining("Invalid");

            // vérifier que la tentative est enregistrée
            verify(bruteForceProtectionService)
                    .registerFailedAttempt("ahmed@email.com");
        }

        @Test
        @DisplayName("Login mauvais mot de passe → InvalidCredentialsException")
        void login_wrongPassword_shouldThrow() {
            // GIVEN
            when(bruteForceProtectionService
                    .isBlocked(anyString()))
                    .thenReturn(false);
            when(userRepository.findByEmail("ahmed@email.com"))
                    .thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(
                    "Password123!",
                    testUser.getPasswordHash()))
                    .thenReturn(false);
            when(bruteForceProtectionService
                    .isBlocked("ahmed@email.com"))
                    .thenReturn(false);
            when(bruteForceProtectionService
                    .getRemainingAttempts("ahmed@email.com"))
                    .thenReturn(3);

            // WHEN + THEN
            assertThatThrownBy(() ->
                    authService.login(loginRequest)
            )
                    .isInstanceOf(InvalidCredentialsException.class)
                    .hasMessageContaining("3");

            // compteur incrémenté
            verify(bruteForceProtectionService)
                    .registerFailedAttempt("ahmed@email.com");
        }

        @Test
        @DisplayName("Login réussi → compteur d'échecs reset")
        void login_success_shouldResetFailedAttempts() {
            // GIVEN
            when(bruteForceProtectionService
                    .isBlocked(anyString()))
                    .thenReturn(false);
            when(userRepository.findByEmail(anyString()))
                    .thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(anyString(), anyString()))
                    .thenReturn(true);
            when(jwtService.generateToken(any()))
                    .thenReturn("token");
            when(refreshTokenService.createRefreshToken(any()))
                    .thenReturn("refresh");

            // WHEN
            authService.login(loginRequest);

            // THEN
            verify(bruteForceProtectionService)
                    .resetAttempts("ahmed@email.com");
        }
    }

    // ════════════════════════════════════
    // BRUTE FORCE TESTS
    // ════════════════════════════════════

    @Nested
    @DisplayName("Protection Brute Force")
    class BruteForceTests {

        @Test
        @DisplayName("5 tentatives échouées → compteur incrémenté 5 fois")
        void fiveFailedAttempts_shouldRegisterEachAttempt() {
            // GIVEN
            when(bruteForceProtectionService
                    .isBlocked(anyString()))
                    .thenReturn(false);
            when(userRepository.findByEmail(anyString()))
                    .thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(anyString(), anyString()))
                    .thenReturn(false);
            when(bruteForceProtectionService
                    .getRemainingAttempts(anyString()))
                    .thenReturn(4, 3, 2, 1, 0);

            // WHEN — 5 tentatives échouées
            for (int i = 0; i < 5; i++) {
                try {
                    authService.login(loginRequest);
                } catch (InvalidCredentialsException e) {
                    // expected
                }
            }

            // THEN — registerFailedAttempt appelé 5 fois
            verify(bruteForceProtectionService, times(5))
                    .registerFailedAttempt("ahmed@email.com");
        }

        @Test
        @DisplayName("Compte bloqué → message avec minutes restantes")
        void blockedAccount_shouldShowRemainingMinutes() {
            // GIVEN
            when(bruteForceProtectionService
                    .isBlocked("ahmed@email.com"))
                    .thenReturn(true);
            when(bruteForceProtectionService
                    .getBlockedMinutesRemaining("ahmed@email.com"))
                    .thenReturn(8L);

            // WHEN + THEN
            assertThatThrownBy(() ->
                    authService.login(loginRequest)
            )
                    .isInstanceOf(AccountBlockedException.class)
                    .hasMessageContaining("8");
        }

        @Test
        @DisplayName("Mauvais mot de passe qui déclenche le blocage → AccountBlockedException")
        void wrongPasswordTriggersBlock_shouldThrowAccountBlocked() {
            // GIVEN — this covers the branch in login() where the failed
            // attempt itself causes the account to become blocked
            when(bruteForceProtectionService
                    .isBlocked("ahmed@email.com"))
                    .thenReturn(false) // check at the top of login()
                    .thenReturn(true);  // check right after the failed attempt
            when(userRepository.findByEmail("ahmed@email.com"))
                    .thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(anyString(), anyString()))
                    .thenReturn(false);
            when(bruteForceProtectionService
                    .getBlockedMinutesRemaining("ahmed@email.com"))
                    .thenReturn(15L);

            // WHEN + THEN
            assertThatThrownBy(() ->
                    authService.login(loginRequest)
            )
                    .isInstanceOf(AccountBlockedException.class)
                    .hasMessageContaining("15");

            verify(bruteForceProtectionService)
                    .registerFailedAttempt("ahmed@email.com");
        }
    }
}