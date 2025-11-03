package dev.CaoNguyen_1883.ecommerce.auth.service;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;

public interface IJwtService {
    /**
     * Generate access token for user
     */
    String generateToken(UserDetails userDetails);

    /**
     * Generate access token with extra claims
     */
    String generateToken(Map<String, Object> extraClaims, UserDetails userDetails);

    /**
     * Generate refresh token
     */
    String generateRefreshToken(UserDetails userDetails);

    /**
     * Extract username (email) from token
     */
    String extractUsername(String token);

    /**
     * Validate token against user details
     */
    boolean isTokenValid(String token, UserDetails userDetails);

    /**
     * Check if token is expired
     */
    boolean isTokenExpired(String token);
}
