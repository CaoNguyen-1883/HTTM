package dev.CaoNguyen_1883.ecommerce.seed;

import dev.CaoNguyen_1883.ecommerce.user.entity.Role;
import dev.CaoNguyen_1883.ecommerce.user.entity.User;
import dev.CaoNguyen_1883.ecommerce.user.repository.RoleRepository;
import dev.CaoNguyen_1883.ecommerce.user.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Order(11)
@RequiredArgsConstructor
@Slf4j
public class CustomerSeeder implements Seeder {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String DEFAULT_PASSWORD = "User123";

    // Vietnamese names for realistic data
    private static final String[] FIRST_NAMES = {
        "Minh",
        "Hoa",
        "Lan",
        "Tuan",
        "Linh",
        "Dung",
        "Hai",
        "Mai",
        "Nam",
        "Huong",
        "Thanh",
        "Phuong",
        "Trang",
        "Anh",
        "Duc",
        "Ngoc",
        "Khang",
        "Nhi",
        "Quang",
        "Vy",
        "Bao",
        "Chi",
        "Dat",
        "Giang",
        "Hoang",
        "Khanh",
        "Long",
        "My",
        "Nhung",
        "Phuc",
        "Quan",
        "Thu",
        "Tien",
        "Uyen",
        "Van",
        "Xuan",
        "Yen",
        "An",
        "Binh",
        "Cuong",
        "Diep",
        "Em",
        "Giao",
        "Ha",
        "Hang",
        "Hieu",
        "Kien",
        "Lam",
        "Le",
        "Ly",
    };

    private static final String[] LAST_NAMES = {
        "Nguyen",
        "Tran",
        "Le",
        "Pham",
        "Hoang",
        "Phan",
        "Vu",
        "Vo",
        "Dang",
        "Bui",
        "Do",
        "Ho",
        "Ngo",
        "Duong",
        "Ly",
        "Mai",
        "Truong",
        "Trinh",
        "Dinh",
        "Ta",
    };

    @Override
    public void seed() {
        log.info("Starting customer account seeding...");

        // Check total user count
        long totalUsers = userRepository.count();
        log.info("Current total users in database: {}", totalUsers);

        // Skip if we already have many users (threshold: 120 users total)
        if (totalUsers >= 120) {
            log.info(
                "Customers already seeded (total users: {}), skipping",
                totalUsers
            );
            return;
        }

        // Find CUSTOMER role
        Role customerRole = roleRepository
            .findByName("ROLE_CUSTOMER")
            .orElseThrow(() ->
                new RuntimeException("ROLE_CUSTOMER not found in database")
            );

        // Encode password once
        String encodedPassword = passwordEncoder.encode(DEFAULT_PASSWORD);

        List<User> customers = new ArrayList<>();
        Random random = new Random();

        int targetCount = 120; // Generate 120 customers
        int created = 0;
        int skipped = 0;

        log.info("Generating {} customer accounts...", targetCount);

        for (int i = 1; i <= targetCount; i++) {
            try {
                // Generate random Vietnamese name
                String firstName = FIRST_NAMES[random.nextInt(
                    FIRST_NAMES.length
                )];
                String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
                String fullName = lastName + " " + firstName;

                // Generate email from name (lowercase, no spaces)
                String emailPrefix = (lastName.toLowerCase() +
                    firstName.toLowerCase()).replaceAll("\\s+", "");
                String email = emailPrefix + i + "@customer.com";

                // Check if email already exists
                if (userRepository.existsByEmail(email)) {
                    log.debug("Email already exists: {}", email);
                    skipped++;
                    continue;
                }

                // Create user
                User customer = User.builder()
                    .email(email)
                    .password(encodedPassword)
                    .fullName(fullName)
                    .phone(generatePhone())
                    .emailVerified(random.nextBoolean()) // Random verification status
                    .build();

                customer.assignRole(customerRole);
                customers.add(customer);
                created++;

                // Batch save every 50 users
                if (customers.size() >= 50) {
                    userRepository.saveAll(customers);
                    log.info("Saved {} customers...", created);
                    customers.clear();
                }
            } catch (Exception e) {
                log.error("Error creating customer {}: {}", i, e.getMessage());
                skipped++;
            }
        }

        // Save remaining customers
        if (!customers.isEmpty()) {
            userRepository.saveAll(customers);
        }

        log.info(
            "Customer seeding completed. Created: {}, Skipped: {}",
            created,
            skipped
        );
        log.info("Default password for all customers: {}", DEFAULT_PASSWORD);
    }

    private String generatePhone() {
        Random random = new Random();
        // Vietnamese phone format: 09x or 03x followed by 8 digits
        String prefix = random.nextBoolean() ? "09" : "03";
        long number = 10000000L + (long) (random.nextDouble() * 90000000L);
        return prefix + number;
    }
}
