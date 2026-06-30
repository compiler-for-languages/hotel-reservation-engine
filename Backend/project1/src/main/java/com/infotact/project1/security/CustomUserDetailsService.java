package com.infotact.project1.security;

import com.infotact.project1.enums.AccountStatus;
import com.infotact.project1.model.User;
import com.infotact.project1.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

/*
 * Custom implementation of Spring Security's UserDetailsService.
 *
 * Responsible for loading user information from the database
 * during authentication.
 *
 * Spring Security calls this service whenever a user attempts
 * to authenticate using email and password or a JWT token.
 */

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    // Repository used to fetch user records from the database
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) {

        // Retrieve user using email address
        User user = userRepository.findByEmail(email)

                // Thrown when no user exists with given email
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found"));

        // Allow login only for active accounts
        if (user.getAccountStatus() != AccountStatus.ACTIVE) {

            throw new RuntimeException(
                    "Account is inactive");
        }

        /*
         * Convert application User entity into Spring Security
         * UserDetails object.
         *
         * Spring Security uses:
         * - Email as username
         * - Password hash for verification
         * - Authorities for authorization
         */

        return new org.springframework.security.core.userdetails.User(
                // Username used by spring security
                user.getEmail(),

                // Bcrypt hashed password stored in database
                user.getPasswordHash(),

                // User roles converted into Spring authorities
                List.of(
                        new SimpleGrantedAuthority(
                                // Example:
                                // CUSTOMER -> ROLE_CUSTOMER
                                // ADMIN -> ROLE_ADMIN
                                "ROLE_" + user.getRole().name()
                        )
                )
        );
    }
}