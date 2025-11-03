package dev.CaoNguyen_1883.ecommerce.seed;

import dev.CaoNguyen_1883.ecommerce.user.entity.AuthProvider;
import dev.CaoNguyen_1883.ecommerce.seed.constants.RoleConstants;
import dev.CaoNguyen_1883.ecommerce.user.entity.Role;
import dev.CaoNguyen_1883.ecommerce.user.entity.User;
import dev.CaoNguyen_1883.ecommerce.user.repository.RoleRepository;
import dev.CaoNguyen_1883.ecommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@Order(4)
@RequiredArgsConstructor
@Slf4j
public class AdminSeeder implements Seeder {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void seed() {
        log.info("Starting admin account seeding...");

        String adminEmail = "admin@ecommerce.com";

        if (userRepository.existsByEmail(adminEmail)) {
            log.info("Admin account already exists, skipping...");
            return;
        }

        // Get ROLE_ADMIN
        Role adminRole = roleRepository.findByName(RoleConstants.ROLE_ADMIN)
                .orElseThrow(() -> new RuntimeException("ROLE_ADMIN not found. Please seed roles first."));

        // Create admin user
        User admin = User.builder()
                .email(adminEmail)
                .fullName("System Administrator")
                    .password(passwordEncoder.encode("Admin@123"))
                .provider(AuthProvider.LOCAL)
                .emailVerified(true)
                .phone("0123456789")
                .roles(Set.of(adminRole))
                .build();

        userRepository.save(admin);

        log.info("========================================");
        log.info("Admin account created successfully!");
        log.info("Email: {}", adminEmail);
        log.info("Password: Admin@123");
        log.info("IMPORTANT: Please change this password after first login!");
        log.info("========================================");
    }
}
