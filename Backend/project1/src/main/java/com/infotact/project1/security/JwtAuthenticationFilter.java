package com.infotact.project1.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/*
 * JWT authentication filter.
 *
 * Executes once for every incoming HTTP request.
 *
 * Responsibilities:
 * - Extract JWT token from Authorization header
 * - Validate the token
 * - Load user details from database
 * - Authenticate the user
 * - Store authentication information in Spring Security Context
 *
 * If authentication succeeds, the request is allowed
 * to access protected APIs.
 */

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // Service used for JWT extraction and validation
    private final JwtService jwtService;

    // Loads user details from database using email/username
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        // Read Authorization header from incoming request
        String authHeader =
                request.getHeader("Authorization");

        String token = null;
        String email = null;

        // Extract token from Bearer header
        if (authHeader != null
                && authHeader.startsWith("Bearer ")) {

            // Remove "Bearer" prefix and keep only token
            token = authHeader.substring(7);

            // Extract username/email from token payload
            email = jwtService.extractUsername(token);
        }

        // Authenticate only if not already authenticated
        if (email != null
                && SecurityContextHolder
                .getContext()
                .getAuthentication() == null) {

            //  Load user details from database
            UserDetails userDetails =
                    customUserDetailsService
                            .loadUserByUsername(email);

            //  verify token validity and ownership
            if (jwtService.isTokenValid(
                    token,
                    userDetails.getUsername())) {

                // Create Spring security authentication object
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                // Attach request-specific details
                authToken.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request));

                // Store authenticated user in Security Context
                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authToken);
            }
        }

        // Continue request processing through remaining filters
        // and eventually reach the target controller
        filterChain.doFilter(request, response);
    }
}