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

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http)
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

                        // Public APIs
                        .requestMatchers(
                                "/api/auth/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()



                                // ---------------- ADMIN ----------------

                                .requestMatchers("/api/admin/users/**")
                                .hasRole("ADMIN")

                                .requestMatchers("/api/admin/room/**")
                                .hasRole("ADMIN")

                                .requestMatchers("/api/admin/roomtype/**")
                                .hasRole("ADMIN")

                                // ---------------- RECEPTION ----------------

                        // ---------------- CUSTOMER / COMMON ----------------

                        .requestMatchers(
                                "/api/reservation/**",
                                "/api/payment/**",
                                "/api/guest/**",
                                "/api/availability/**",
                                "/api/bookinghold/**"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "RECEPTIONIST",
                                "CUSTOMER"
                        )

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
 Our frontend runs on

http://localhost:5173

and your backend runs on

http://localhost:8080

These are different origins (different ports).

Browsers enforce the Same-Origin Policy, so by default they block JavaScript from calling another origin

"Use my CORS configuration and allow approved frontend applications to access my backend."

without this

No 'Access-Control-Allow-Origin' header is present
     */

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config)
            throws Exception {

        return config.getAuthenticationManager();
    }
}