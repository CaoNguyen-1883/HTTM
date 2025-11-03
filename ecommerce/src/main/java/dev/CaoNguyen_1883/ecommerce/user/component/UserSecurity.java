package dev.CaoNguyen_1883.ecommerce.user.component;

import dev.CaoNguyen_1883.ecommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("userSecurity")
@RequiredArgsConstructor
public class UserSecurity {
    
    private final UserRepository userRepository;
    
    public boolean isSelf(Authentication authentication, UUID userId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        if (!(authentication.getPrincipal() instanceof Jwt jwt)) {
            return false;
        }
        
        String email = jwt.getSubject();
        
        return userRepository.findByEmail(email)
                .map(user -> user.getId().equals(userId))
                .orElse(false);
    }
    
    public boolean isSelfByEmail(Authentication authentication, String email) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        if (!(authentication.getPrincipal() instanceof Jwt jwt)) {
            return false;
        }
        
        String currentUserEmail = jwt.getSubject();
        return currentUserEmail.equals(email);
    }

    public boolean isAdminOrSelf(Authentication authentication, UUID userId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        if (!(authentication.getPrincipal() instanceof Jwt jwt)) {
            return false;
        }

        // Check if user has ADMIN role
        var authorities = jwt.getClaimAsStringList("roles");
        if (authorities != null && authorities.contains("ROLE_ADMIN")) {
            return true;
        }

        // Check if it's the same user
        String email = jwt.getSubject();
        return userRepository.findByEmail(email)
                .map(user -> user.getId().equals(userId))
                .orElse(false);
    }
}