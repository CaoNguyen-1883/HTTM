package dev.CaoNguyen_1883.ecommerce.seed;

import dev.CaoNguyen_1883.ecommerce.seed.constants.RoleConstants;
import dev.CaoNguyen_1883.ecommerce.user.entity.AuthProvider;
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
@Order(5)
@RequiredArgsConstructor
@Slf4j
public class SellerSeeder implements Seeder {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void seed() {
        log.info("Starting seller account seeding...");

        String sellerEmail = "seller@ecommerce.com";

        if (userRepository.existsByEmail(sellerEmail)) {
            log.info("Seller account already exists, skipping...");
            return;
        }

        // Get ROLE_SELLER
        Role sellerRole = roleRepository.findByName(RoleConstants.ROLE_SELLER)
                .orElseThrow(() -> new RuntimeException("ROLE_SELLER not found. Please seed roles first."));

        // Create seller user
        User seller = User.builder()
                .email(sellerEmail)
                .fullName("Demo Seller")
                .password(passwordEncoder.encode("Seller@123"))
                .provider(AuthProvider.LOCAL)
                .emailVerified(true)
                .phone("0987654321")
                .roles(Set.of(sellerRole))
                .build();

        userRepository.save(seller);

        log.info("========================================");
        log.info("Seller account created successfully!");
        log.info("Email: {}", sellerEmail);
        log.info("Password: Seller@123");
        log.info("========================================");
    }
}
