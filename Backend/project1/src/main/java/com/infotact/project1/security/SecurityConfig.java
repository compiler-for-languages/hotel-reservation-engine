package com.infotact.project1.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/*
 * Central Security Configuration
 *
 * Responsibilities:
 * -> Defines API authorization rules
 * -> Configures JWT authentication
 * -> Registers JWT authentication filter
 * -> Enables CORS
 * -> Disables CSRF (JWT authentication is stateless)
 * -> Creates AuthenticationManager bean
 */

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /*
     * Custom JWT filter responsible for:
     * -> Extracting JWT token from Authorization header
     * -> Validating JWT
     * -> Authenticating the user
     */
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /*
     * Defines security rules for all incoming HTTP requests.
     * Rules are evaluated top-to-bottom.
     * First matching rule wins.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http

                // Enable CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Disable CSRF for JWT authentication
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth

                        // Allow browser preflight requests
                        .requestMatchers(HttpMethod.OPTIONS, "/**")
                        .permitAll()

                        // ---------------- PUBLIC ----------------

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/register",
                                "/api/auth/login"
                        ).permitAll()

                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/error"
                        ).permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/auth/me")
                        .hasAnyRole(
                                "ADMIN",
                                "RECEPTIONIST",
                                "CUSTOMER"
                        )

                        // ---------------- PROFILE ----------------

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/admin/users/update/*"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "RECEPTIONIST",
                                "CUSTOMER"
                        )

                        // ---------------- ADMIN ----------------

                        .requestMatchers("/api/admin/users/**")
                        .hasRole("ADMIN")

                        .requestMatchers("/api/admin/room/**")
                        .hasRole("ADMIN")

                        // ---------------- ROOM TYPE ----------------

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/admin/roomtype/getall",
                                "/api/admin/roomtype/get",
                                "/api/admin/roomtype/get/*"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "RECEPTIONIST",
                                "CUSTOMER"
                        )

                        .requestMatchers("/api/admin/roomtype/**")
                        .hasRole("ADMIN")

                        // ---------------- RECEPTION ----------------

                        .requestMatchers("/api/reception/**")
                        .hasAnyRole(
                                "ADMIN",
                                "RECEPTIONIST"
                        )

                        // ---------------- RESERVATIONS ----------------

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/reservation/save"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "CUSTOMER"
                        )

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/reservation/getbyuser"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "RECEPTIONIST",
                                "CUSTOMER"
                        )

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/reservation/get/*"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "RECEPTIONIST",
                                "CUSTOMER"
                        )

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/reservation/delete/*"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "CUSTOMER"
                        )

                        .requestMatchers("/api/reservation/**")
                        .hasAnyRole(
                                "ADMIN",
                                "RECEPTIONIST"
                        )

                        // ---------------- PAYMENTS ----------------

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/payment/save"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "CUSTOMER"
                        )

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/payment/reservation/*"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "RECEPTIONIST",
                                "CUSTOMER"
                        )

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/payment/start/*",
                                "/api/payment/success/*",
                                "/api/payment/fail/*"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "CUSTOMER"
                        )

                        .requestMatchers("/api/payment/**")
                        .hasAnyRole(
                                "ADMIN",
                                "RECEPTIONIST"
                        )

                        // ---------------- GUESTS ----------------

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/guest/save"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "RECEPTIONIST",
                                "CUSTOMER"
                        )

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/guest/getbyreservation"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "RECEPTIONIST",
                                "CUSTOMER"
                        )

                        .requestMatchers("/api/guest/**")
                        .hasAnyRole(
                                "ADMIN",
                                "RECEPTIONIST"
                        )

                        // ---------------- AVAILABILITY ----------------

                        .requestMatchers("/api/availability/**")
                        .hasAnyRole(
                                "ADMIN",
                                "RECEPTIONIST",
                                "CUSTOMER"
                        )

                        // ---------------- BOOKING HOLD ----------------

                        .requestMatchers("/api/bookinghold/**")
                        .hasAnyRole(
                                "ADMIN",
                                "RECEPTIONIST",
                                "CUSTOMER"
                        )

                        // ---------------- DEVELOPMENT ----------------

                        .requestMatchers("/api/lock/**")
                        .permitAll()

                        .anyRequest()
                        .authenticated()
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    /*
     * Configures Cross-Origin Resource Sharing (CORS).
     *
     * Frontend:
     *     http://localhost:5173
     *
     * Backend:
     *     http://localhost:8080
     *
     * Since they run on different origins (different ports),
     * browsers enforce the Same-Origin Policy.
     *
     * This configuration allows approved frontend applications
     * to communicate with the backend.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        // Frontend URL
        configuration.setAllowedOriginPatterns(
                Arrays.asList("http://localhost:5173")
        );

        configuration.setAllowedMethods(
                Arrays.asList(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS"
                )
        );

        configuration.addAllowedHeader("*");

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }

    /*
     * Our frontend runs on:
     *
     * http://localhost:5173
     *
     * Backend:
     *
     * http://localhost:8080
     *
     * Since these are different origins,
     * browsers enforce the Same-Origin Policy.
     *
     * Without this CORS configuration you'll get:
     *
     * No 'Access-Control-Allow-Origin' header is present
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config)
            throws Exception {

        return config.getAuthenticationManager();
    }
}
//with appropriate rba protection