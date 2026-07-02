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

     *

     * Rules are evaluated top-to-bottom; the first match wins.

     * More specific path/method rules must appear before broader ones.

     */

    @Bean

    public SecurityFilterChain securityFilterChain(HttpSecurity http)

            throws Exception {



        http



                /*

                 * Enable Cross-Origin Resource Sharing (CORS)

                 */

                .cors(cors -> cors.configurationSource(corsConfigurationSource()))



                /*

                 * Disable CSRF protection.

                 *

                 * Since JWT authentication is stateless and does not use

                 * server-side sessions, CSRF protection is unnecessary.

                 */

                .csrf(csrf -> csrf.disable())



                .authorizeHttpRequests(auth -> auth



                        /*

                         * Allow browser preflight requests.

                         */

                        .requestMatchers(HttpMethod.OPTIONS, "/**")

                        .permitAll()



                        // ---------------- PUBLIC ----------------



                        .requestMatchers(

                                HttpMethod.POST,
                                "/api/auth/register",
                                "/api/auth/login",

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



                        // ---------------- PROFILE (ALL ROLES) ----------------

                        /*

                         * Profile pages (customer/admin) reuse the existing user

                         * update API. Ownership and field-level restrictions are

                         * enforced in UserService.updateUser().

                         */

                        .requestMatchers(HttpMethod.PATCH, "/api/admin/users/update/*")

                        .hasAnyRole(

                                "ADMIN",

                                "RECEPTIONIST",

                                "CUSTOMER"

                        )



                        // ---------------- ADMIN USER MANAGEMENT ----------------



                        .requestMatchers("/api/admin/users/**")

                        .hasRole("ADMIN")



                        // ---------------- ADMIN ROOM MANAGEMENT ----------------



                        .requestMatchers("/api/admin/room/**")

                        .hasRole("ADMIN")



                        // ---------------- ROOM TYPE READ (SHARED) ----------------

                        /*

                         * Read-only room type data is required by customers

                         * (Search Rooms) and reception staff. Write operations

                         * remain admin-only via the rule below.

                         */

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



                        // ---------------- ADMIN ROOM TYPE MANAGEMENT ----------------



                        .requestMatchers("/api/admin/roomtype/**")

                        .hasRole("ADMIN")



                        // ---------------- RECEPTION OPERATIONS ----------------



                        .requestMatchers("/api/reception/**")

                        .hasAnyRole(

                                "ADMIN",

                                "RECEPTIONIST"

                        )



                        // ---------------- RESERVATIONS (ROLE-SPECIFIC) ----------------

                        /*

                         * Customers: create bookings, view/cancel own reservations.

                         * Admin/Reception: full reservation management.

                         */

                        .requestMatchers(HttpMethod.POST, "/api/reservation/save")

                        .hasAnyRole(

                                "ADMIN",

                                "CUSTOMER"

                        )



                        .requestMatchers(HttpMethod.GET, "/api/reservation/getbyuser")

                        .hasAnyRole(

                                "ADMIN",

                                "RECEPTIONIST",

                                "CUSTOMER"

                        )



                        .requestMatchers(HttpMethod.GET, "/api/reservation/get/*")

                        .hasAnyRole(

                                "ADMIN",

                                "RECEPTIONIST",

                                "CUSTOMER"

                        )



                        .requestMatchers(HttpMethod.DELETE, "/api/reservation/delete/*")

                        .hasAnyRole(

                                "ADMIN",

                                "CUSTOMER"

                        )



                        .requestMatchers("/api/reservation/**")

                        .hasAnyRole(

                                "ADMIN",

                                "RECEPTIONIST"

                        )



                        // ---------------- PAYMENTS (ROLE-SPECIFIC) ----------------

                        /*

                         * Customers: create and complete payments for own bookings.

                         * Admin/Reception: full payment management and reporting.

                         */

                        .requestMatchers(HttpMethod.POST, "/api/payment/save")

                        .hasAnyRole(

                                "ADMIN",

                                "CUSTOMER"

                        )



                        .requestMatchers(HttpMethod.GET, "/api/payment/reservation/*")

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



                        // ---------------- GUEST MANAGEMENT ----------------

                        /*
                         * Customers can add and view guests for their own reservations.
                         * Reception and admin retain full guest management.
                         */
                        .requestMatchers(HttpMethod.POST, "/api/guest/save")
                        .hasAnyRole(
                                "ADMIN",
                                "RECEPTIONIST",
                                "CUSTOMER"
                        )

                        .requestMatchers(HttpMethod.GET, "/api/guest/getbyreservation")
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



                        // ---------------- AVAILABILITY / BOOKING HOLD ----------------



                        .requestMatchers("/api/availability/**")

                        .hasAnyRole(

                                "ADMIN",

                                "RECEPTIONIST",

                                "CUSTOMER"

                        )



                        .requestMatchers("/api/bookinghold/**")

                        .hasAnyRole(

                                "ADMIN",

                                "RECEPTIONIST",

                                "CUSTOMER"

                        )



                        // ---------------- DEVELOPMENT ----------------



                        .requestMatchers("/api/lock/**")

                        .permitAll()



                        /*

                         * Any endpoint not explicitly matched above

                         * requires authentication.

                         */

                        .anyRequest()

                        .authenticated()

                )



                /*

                 * Insert JWT filter before Spring Security's

                 * UsernamePasswordAuthenticationFilter.

                 *

                 * JWT authentication must occur before the default

                 * username/password authentication filter.

                 */

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

     * Creates AuthenticationManager bean.

     *

     * Used during login to authenticate

     * username and password credentials.

     */

    @Bean

    public AuthenticationManager authenticationManager(

            AuthenticationConfiguration config)

            throws Exception {



        return config.getAuthenticationManager();

    }

}


