package com.infotact.project1.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/*
 * Registers BCrypt password encoder as a Spring Bean.
 * Used to hash passwords before storing them in the database.
 */

@Configuration
public class PasswordConfig {

    // Creates a reusable password encoder object
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }
}