package dev.CaoNguyen_1883.ecommerce.seed;

import dev.CaoNguyen_1883.ecommerce.seed.constants.PermissionConstants;
import dev.CaoNguyen_1883.ecommerce.user.entity.Permission;
import dev.CaoNguyen_1883.ecommerce.user.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class PermissionSeeder implements Seeder {

    private final PermissionRepository permissionRepository;

    @Override
    public void seed() {
        log.info("Starting permission seeding...");

        Map<String, String> permissions = getPermissionsMap();
        int created = 0;
        int skipped = 0;

        for (Map.Entry<String, String> entry : permissions.entrySet()) {
            if (permissionRepository.existsByName(entry.getKey())) {
                skipped++;
                log.debug("Permission already exists: {}", entry.getKey());
            } else {
                Permission permission = Permission.builder()
                        .name(entry.getKey())
                        .description(entry.getValue())
                        .build();
                permissionRepository.save(permission);
                created++;
                log.debug("Created permission: {}", entry.getKey());
            }
        }

        log.info("Permission seeding completed. Created: {}, Skipped: {}", created, skipped);
    }

    private Map<String, String> getPermissionsMap() {
        Map<String, String> permissions = new HashMap<>();

        // User permissions
        permissions.put(PermissionConstants.USER_VIEW, "View users");
        permissions.put(PermissionConstants.USER_CREATE, "Create new users");
        permissions.put(PermissionConstants.USER_UPDATE, "Update user information");
        permissions.put(PermissionConstants.USER_DELETE, "Delete users");

        // Product permissions
        permissions.put(PermissionConstants.PRODUCT_VIEW, "View products");
        permissions.put(PermissionConstants.PRODUCT_CREATE, "Create new products");
        permissions.put(PermissionConstants.PRODUCT_UPDATE, "Update products");
        permissions.put(PermissionConstants.PRODUCT_DELETE, "Delete products");

        // Order permissions
        permissions.put(PermissionConstants.ORDER_VIEW, "View orders");
        permissions.put(PermissionConstants.ORDER_UPDATE, "Update order status");
        permissions.put(PermissionConstants.ORDER_APPROVE, "Approve customer orders");
        permissions.put(PermissionConstants.ORDER_CANCEL, "Cancel orders");

        // Category permissions
        permissions.put(PermissionConstants.CATEGORY_VIEW, "View product categories");
        permissions.put(PermissionConstants.CATEGORY_CREATE, "Create categories");
        permissions.put(PermissionConstants.CATEGORY_UPDATE, "Update categories");
        permissions.put(PermissionConstants.CATEGORY_DELETE, "Delete categories");

        // Brand permissions
        permissions.put(PermissionConstants.BRAND_VIEW, "View brands");
        permissions.put(PermissionConstants.BRAND_CREATE, "Create new brands");
        permissions.put(PermissionConstants.BRAND_UPDATE, "Update brands");
        permissions.put(PermissionConstants.BRAND_DELETE, "Delete brands");

        // Review permissions
        permissions.put(PermissionConstants.REVIEW_VIEW, "View product reviews");
        permissions.put(PermissionConstants.REVIEW_CREATE, "Add product reviews");
        permissions.put(PermissionConstants.REVIEW_DELETE, "Delete product reviews");

        // Payment permissions
        permissions.put(PermissionConstants.PAYMENT_VIEW, "View payment info");
        permissions.put(PermissionConstants.PAYMENT_UPDATE, "Update payment info");

        // Analytics permissions
        permissions.put(PermissionConstants.RECOMMENDATION_VIEW, "View recommendation data");
        permissions.put(PermissionConstants.ANALYTICS_VIEW, "View analytics and reports");

        return permissions;
    }
}
