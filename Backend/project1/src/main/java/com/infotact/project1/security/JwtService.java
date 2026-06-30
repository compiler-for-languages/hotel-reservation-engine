package com.infotact.project1.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

/*
 * JWT utility service.
 *
 * Responsible for:
 * - Generating JWT tokens
 * - Extracting information from tokens
 * - Validating tokens
 * - Checking token expiration
 * - Creating signing keys
 *
 * Used by:
 * - AuthService during login
 * - JwtAuthenticationFilter during request authentication
 */

@Service
public class JwtService {

    // Secret key used to sign and verify JWT tokens
    @Value("${jwt.secret}") // value available in application.properties
    private String secretKey;

    // Token validity duration in milliseconds
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /*
     * Generates a JWT token for an authenticated user.
     *
     * Token contains:
     * - Subject (user email)
     * - Issued timestamp
     * - Expiration timestamp
     * - Digital signature
     */
    public String generateToken(String email) {

        //JWT token = header + payload + signature
        return Jwts.builder() // automatically creates header
                // Stores authenticated user's email
                .subject(email) // payload component

                // Token creation time
                .issuedAt(new Date()) // payload component

                // Token expiration time
                .expiration(
                        new Date(
                                System.currentTimeMillis()
                                        + jwtExpiration // payload component
                        )
                )
                // Sign token using secret key
                .signWith(getSigningKey()) // signature component

                // Generate final JWT string
                .compact();
    }

    /*
     * Extracts username/email from token payload.
     *
     * Example:
     * Token -> john@gmail.com
     */
    public String extractUsername(String token) {

        return extractClaim(
                token,
                Claims::getSubject
        );
    }

    /*
     * Validates token ownership and expiration.
     *
     * Checks:
     * - Username matches expected user
     * - Token has not expired
     */
    public boolean isTokenValid(
            String token,
            String email) {

        return email.equals(extractUsername(token))
                && !isTokenExpired(token);
    }

    /*
     * Generic helper method used to extract any claim
     * from JWT payload.
     *
     * Examples:
     * - Subject
     * - Expiration
     * - IssuedAt
     */
    public <T> T extractClaim(
            String token,
            Function<Claims, T> resolver) {

        Claims claims = extractAllClaims(token);

        return resolver.apply(claims);
    }

    /*
     * Parses JWT token and retrieves all claims.
     *
     * Also verifies token signature using secret key.
     */
    private Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /*
     * Checks whether token expiration time
     * has already passed.
     */
    private boolean isTokenExpired(String token) {

        return extractClaim(
                token,
                Claims::getExpiration
        ).before(new Date());
    }

    /*
     * Converts application secret string
     * into a cryptographic signing key.
     *
     * Used for:
     * - Token generation
     * - Token verification
     */
    private SecretKey getSigningKey() {

        return Keys.hmacShaKeyFor(
                secretKey.getBytes(
                        StandardCharsets.UTF_8
                )
        );
    }
}