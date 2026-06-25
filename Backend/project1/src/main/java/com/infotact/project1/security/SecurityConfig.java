package com.infotact.project1.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/*
 * Central security configuration
 * Responsibilities:
 * -> Defines API access rules
 * -> Configures JWT authentication
 * -> Regsiters security filters
 * -> Controls which endpoints require authentication
 * -> Creates AuthenticationManager bean
 */


@Configuration

/*
* Enables method - level security annotations such as :
* @PreAuthorize(...)
* @PostAuthorize(...)
* @Secured(...)
 */
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /*
    * Custom JWT filter responsible for:
    * Extracting JWT token from authorization header
    * Validating token
    * Authenticating the user
     */

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // Defines security rules for incoming HTTP requests
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http)
            throws Exception {

        http
                /*
                * Disable CSRF protection
                * Since this project uses JWT-based authentication
                * instead of session-based authentication,
                * CSRF protection is unnecessary
                 */
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/api/auth/**"   // publicly accessible
                        ).permitAll()

                        .requestMatchers( //swagger documentation endpoints are publicly
                                            // accessible
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()
                         //temporary, Permitting all for development stage
                        .anyRequest().permitAll()
//                        .authenticated()
                        //except the register and login API, Every other API needs Bearer token if .authenticate id written,

                )

                /*
                * Insert JWT filter before Spring's default
                * UsernamePasswordAuthenticationFilter
                *
                * * JWT authentication should happen first
                 */

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    /*
    * Creates AuthenticationManager bean.
    *
    * Used during login to authenticate
    * Username and password credentials
     */

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config)
            throws Exception {

        return config.getAuthenticationManager();
    }
}