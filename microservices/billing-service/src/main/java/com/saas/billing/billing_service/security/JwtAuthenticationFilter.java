package com.saas.billing.billing_service.security;


import com.saas.billing.billing_service.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        // extract header Authorization
        String authHeader = request.getHeader("Authorization");

        // si pas de header → continue without auth
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // extract token
        String token = authHeader.substring(7);

        // validate token
        if (!jwtService.isTokenValid(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
            // check it out
            /*filterChain.doFilter(request, response);
            return;*/
        }

        // extract infos from token
        String email = jwtService.extractEmail(token);
        String userId = jwtService.extractUserId(token);
        String role = jwtService.extractRole(token);

        AuthenticatedUser authenticatedUser =  new AuthenticatedUser(userId, email);

        // create Authentication object
        // and put it in securityContext
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        authenticatedUser,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );

        // tell Spring that this user is authenticated
        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        // continue to controller
        filterChain.doFilter(request, response);
    }
}


