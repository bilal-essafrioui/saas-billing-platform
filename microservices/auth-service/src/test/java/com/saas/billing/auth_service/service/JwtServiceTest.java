package com.saas.billing.auth_service.service;

import com.saas.billing.auth_service.config.JwtProperties;
import com.saas.billing.auth_service.domain.entity.User;
import com.saas.billing.auth_service.domain.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JwtService Tests")
class JwtServiceTest {

    private JwtService jwtService;

    private static final String TEST_SECRET =
            "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    private User testUser;

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties(
                TEST_SECRET,
                3600000L,
                604800000L
        );

        jwtService = new JwtService(jwtProperties);

        testUser = User.builder()
                .id(UUID.randomUUID())
                .firstName("Ahmed")
                .lastName("Benali")
                .email("ahmed@email.com")
                .role(Role.CUSTOMER)
                .build();
    }

    @Test
    @DisplayName("generateToken → token non null")
    void generateToken_shouldReturnNonNullToken() {
        String token = jwtService.generateToken(testUser);
        assertThat(token).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("generateToken → token valide")
    void generateToken_shouldReturnValidToken() {
        String token = jwtService.generateToken(testUser);
        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    @DisplayName("extractEmail → email correct")
    void extractEmail_shouldReturnCorrectEmail() {
        String token = jwtService.generateToken(testUser);
        String email = jwtService.extractEmail(token);
        assertThat(email).isEqualTo("ahmed@email.com");
    }

    @Test
    @DisplayName("extractRole → rôle correct")
    void extractRole_shouldReturnCorrectRole() {
        String token = jwtService.generateToken(testUser);
        String role = jwtService.extractRole(token);
        assertThat(role).isEqualTo("CUSTOMER");
    }

    @Test
    @DisplayName("extractUserId → userId correct")
    void extractUserId_shouldReturnCorrectUserId() {
        String token = jwtService.generateToken(testUser);
        String userId = jwtService.extractUserId(token);
        assertThat(userId)
                .isEqualTo(testUser.getId().toString());
    }

    @Test
    @DisplayName("token invalide → isTokenValid retourne false")
    void invalidToken_shouldReturnFalse() {
        assertThat(jwtService.isTokenValid("invalid.token.here"))
                .isFalse();
    }

    @Test
    @DisplayName("token expiré → isTokenValid retourne false")
    void expiredToken_shouldReturnFalse() {
        JwtService expiredJwtService = new JwtService(
                new JwtProperties(
                        TEST_SECRET,
                        -1000L,
                        604800000L
                )
        );

        String expiredToken = expiredJwtService.generateToken(testUser);

        assertThat(expiredJwtService.isTokenValid(expiredToken))
                .isFalse();
    }

    @Test
    @DisplayName("token null → isTokenValid retourne false")
    void nullToken_shouldReturnFalse() {
        assertThat(jwtService.isTokenValid(null)).isFalse();
    }
}