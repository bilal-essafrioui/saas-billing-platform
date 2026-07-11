package com.saas.billing.auth_service.config;

import com.saas.billing.auth_service.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // ════════════════════════════════════
    // FILTER CHAIN
    // ════════════════════════════════════
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                // Deactivate CSRF
                .csrf(AbstractHttpConfigurer::disable)

                // No session
                // We use JWT → stateless
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        // endpoints publics
                        // no need to be connected
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/auth/refresh"
                        ).permitAll()

                        // other endpoints
                        // need to be authenticated
                        .anyRequest().authenticated()
                )
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    // ════════════════════════════════════
    // PASSWORD ENCODER
    // ════════════════════════════════════

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt with a cost factor of 12
        // The higher the cost factor, the stronger the security
        // However, it also takes longer to compute
        // A cost factor of 12 provides a good balance between security and performance
        return new BCryptPasswordEncoder(12);
    }
}