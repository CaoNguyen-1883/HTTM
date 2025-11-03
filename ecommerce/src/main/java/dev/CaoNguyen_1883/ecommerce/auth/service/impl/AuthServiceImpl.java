package dev.CaoNguyen_1883.ecommerce.auth.service.impl;


import dev.CaoNguyen_1883.ecommerce.auth.dto.AuthResponse;
import dev.CaoNguyen_1883.ecommerce.auth.dto.LoginRequest;
import dev.CaoNguyen_1883.ecommerce.auth.dto.RefreshTokenRequest;
import dev.CaoNguyen_1883.ecommerce.auth.dto.RegisterRequest;
import dev.CaoNguyen_1883.ecommerce.auth.service.IAuthService;

import dev.CaoNguyen_1883.ecommerce.auth.security.CustomUserDetails;
import dev.CaoNguyen_1883.ecommerce.auth.service.IJwtService;
import dev.CaoNguyen_1883.ecommerce.user.entity.AuthProvider;
import dev.CaoNguyen_1883.ecommerce.common.exception.BadRequestException;
import dev.CaoNguyen_1883.ecommerce.common.exception.DuplicateResourceException;
import dev.CaoNguyen_1883.ecommerce.seed.constants.RoleConstants;
import dev.CaoNguyen_1883.ecommerce.user.entity.Role;
import dev.CaoNguyen_1883.ecommerce.user.entity.User;
import dev.CaoNguyen_1883.ecommerce.user.repository.RoleRepository;
import dev.CaoNguyen_1883.ecommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements IAuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final IJwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getEmail());

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        // Get ROLE_CUSTOMER for new users
        Role customerRole = roleRepository.findByName(RoleConstants.ROLE_CUSTOMER)
                .orElseThrow(() -> new RuntimeException("ROLE_CUSTOMER not found. Please run database seeding."));

        // Create new user
        User user = User.builder()
                .email(request.getEmail())
                .fullName(request.getFullName())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .provider(AuthProvider.LOCAL)
                .emailVerified(true)  // Should verify email later
                .roles(Set.of(customerRole))
                .build();

        User savedUser = userRepository.save(user);

        // Generate tokens
        CustomUserDetails userDetails = new CustomUserDetails(savedUser);
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        log.info("User registered successfully: {}", savedUser.getEmail());

        return buildAuthResponse(savedUser, accessToken, refreshToken);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("User attempting to login: {}, {}", request.getEmail(), request.getPassword());

        // Authenticate user
        // authenticate() call loadUserByUsername
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Load user with roles
        User user = userRepository.findByEmailWithRoles(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if(request.getExpectedRole() != null && !request.getExpectedRole().isEmpty()){
            if(!user.getRoles().stream()
                    .anyMatch(role -> role.getName().equals(request.getExpectedRole()))){
                throw new BadRequestException(
                        String.format("Your account doesn't have %s access. Please use the correct login portal.",
                                request.getExpectedRole())
                );

            }
        }

        //Manual code
//        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
//            throw new BadRequestException("Wrong password");
//        }
        // Check if user is active
        if (!user.getIsActive()) {
            throw new BadRequestException("Account is disabled. Please contact support.");
        }

        // Check if email is verified (optional - can skip for now)
         if (!user.isEmailVerified()) {
             throw new BadRequestException("Email not verified. Please verify your email.");
         }

        // Generate tokens
        CustomUserDetails userDetails = new CustomUserDetails(user);
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        log.info("User logged in successfully: {}", user.getEmail());

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        log.info("Refreshing access token");

        String refreshToken = request.getRefreshToken();

        // Extract username from refresh token
        String userEmail = jwtService.extractUsername(refreshToken);

        if (userEmail == null) {
            throw new BadRequestException("Invalid refresh token");
        }

        // Load user
        User user = userRepository.findByEmailWithRoles(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Validate refresh token
        CustomUserDetails userDetails = new CustomUserDetails(user);
        if (!jwtService.isTokenValid(refreshToken, userDetails)) {
            throw new BadRequestException("Invalid or expired refresh token");
        }

        // Generate new access token
        String newAccessToken = jwtService.generateToken(userDetails);

        log.info("Access token refreshed for user: {}", userEmail);

        return buildAuthResponse(user, newAccessToken, refreshToken);
    }

    // ============================================
    // Helper Methods
    // ============================================

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        AuthResponse.UserInfo userInfo = AuthResponse.UserInfo.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .roles(roleNames)
                .emailVerified(user.isEmailVerified())
                .build();

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .user(userInfo)
                .build();
    }
}