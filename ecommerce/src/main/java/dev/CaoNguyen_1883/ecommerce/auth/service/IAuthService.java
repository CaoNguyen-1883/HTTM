package dev.CaoNguyen_1883.ecommerce.auth.service;


import dev.CaoNguyen_1883.ecommerce.auth.dto.AuthResponse;
import dev.CaoNguyen_1883.ecommerce.auth.dto.LoginRequest;
import dev.CaoNguyen_1883.ecommerce.auth.dto.RefreshTokenRequest;
import dev.CaoNguyen_1883.ecommerce.auth.dto.RegisterRequest;

public interface IAuthService {
    /**
     * Register new user with ROLE_CUSTOMER
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Login with email and password
     */
    AuthResponse login(LoginRequest request);

    /**
     * Refresh access token using refresh token
     */
    AuthResponse refreshToken(RefreshTokenRequest request);
}