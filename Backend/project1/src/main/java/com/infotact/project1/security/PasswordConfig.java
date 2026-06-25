package com.infotact.project1.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/*
 * Password security configuration.
 *
 * Responsible for registering a BCryptPasswordEncoder bean
 * that can be injected throughout the application.
 *
 * Purpose:
 * - Hash passwords before storing them in the database
 * - Prevent plain-text password storage
 * - Verify passwords during login
 */

@Configuration
public class PasswordConfig {
    /*
     * Creates a singleton BCryptPasswordEncoder bean.
     *
     * Spring manages this object and allows it to be injected
     * wherever password encoding or verification is required.
     *
     * Example:
     * private final BCryptPasswordEncoder passwordEncoder;
     */

    // Creates a reusable password encoder object
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {

        /*
         * BCrypt automatically:
         * - Generates a random salt
         * - Hashes the password
         * - Stores salt information within the hash
         * - Protects against rainbow table attacks
         */
        return new BCryptPasswordEncoder();
    }
}