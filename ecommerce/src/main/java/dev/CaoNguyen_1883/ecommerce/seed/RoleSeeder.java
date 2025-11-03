package dev.CaoNguyen_1883.ecommerce.seed;

import dev.CaoNguyen_1883.ecommerce.seed.constants.RoleConstants;
import dev.CaoNguyen_1883.ecommerce.user.entity.Role;
import dev.CaoNguyen_1883.ecommerce.user.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class RoleSeeder implements Seeder {

    private final RoleRepository roleRepository;

    @Override
    public void seed() {
        log.info("Starting role seeding...");

        Map<String, String> roles = getRolesMap();
        int created = 0;
        int skipped = 0;

        for (Map.Entry<String, String> entry : roles.entrySet()) {
            if (roleRepository.existsByName(entry.getKey())) {
                skipped++;
                log.debug("Role already exists: {}", entry.getKey());
            } else {
                Role role = Role.builder()
                        .name(entry.getKey())
                        .description(entry.getValue())
                        .build();
                roleRepository.save(role);
                created++;
                log.debug("Created role: {}", entry.getKey());
            }
        }

        log.info("Role seeding completed. Created: {}, Skipped: {}", created, skipped);
    }

    private Map<String, String> getRolesMap() {
        Map<String, String> roles = new HashMap<>();
        roles.put(RoleConstants.ROLE_ADMIN, "System Administrator with full access");
        roles.put(RoleConstants.ROLE_CUSTOMER, "Customer who can purchase products");
        roles.put(RoleConstants.ROLE_SELLER, "Seller who can manage their products");
        roles.put(RoleConstants.ROLE_STAFF, "Staff member who can approve products and manage orders");
        return roles;
    }
}
