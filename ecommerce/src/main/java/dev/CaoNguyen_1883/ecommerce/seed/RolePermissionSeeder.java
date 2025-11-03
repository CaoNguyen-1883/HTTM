package dev.CaoNguyen_1883.ecommerce.seed;

import dev.CaoNguyen_1883.ecommerce.seed.constants.PermissionConstants;
import dev.CaoNguyen_1883.ecommerce.seed.constants.RoleConstants;
import dev.CaoNguyen_1883.ecommerce.user.entity.Permission;
import dev.CaoNguyen_1883.ecommerce.user.entity.Role;
import dev.CaoNguyen_1883.ecommerce.user.repository.PermissionRepository;
import dev.CaoNguyen_1883.ecommerce.user.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Order(3)
@RequiredArgsConstructor
@Slf4j
public class RolePermissionSeeder implements Seeder {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    public void seed() {
        log.info("Starting role-permission assignment...");

        // Get all permissions as a map for easy lookup
        Map<String, Permission> permissionMap = new HashMap<>();
        permissionRepository.findAll().forEach(p -> permissionMap.put(p.getName(), p));

        // Assign permissions to each role
        assignAdminPermissions(permissionMap);
        assignStaffPermissions(permissionMap);
        assignSellerPermissions(permissionMap);
        assignCustomerPermissions(permissionMap);

        log.info("Role-permission assignment completed");
    }

    private void assignAdminPermissions(Map<String, Permission> permissionMap) {
        Role admin = roleRepository.findByName(RoleConstants.ROLE_ADMIN)
                .orElseThrow(() -> new RuntimeException("ROLE_ADMIN not found"));

        if (admin.getPermissions().isEmpty()) {
            // Admin has all permissions
            admin.setPermissions(new HashSet<>(permissionMap.values()));
            roleRepository.save(admin);
            log.info("Assigned all permissions to ROLE_ADMIN");
        } else {
            log.debug("ROLE_ADMIN already has permissions assigned");
        }
    }

    private void assignStaffPermissions(Map<String, Permission> permissionMap) {
        Role staff = roleRepository.findByName(RoleConstants.ROLE_STAFF)
                .orElseThrow(() -> new RuntimeException("ROLE_STAFF not found"));

        if (staff.getPermissions().isEmpty()) {
            Set<Permission> staffPermissions = new HashSet<>();
            addPermissionIfExists(staffPermissions, permissionMap, PermissionConstants.ORDER_VIEW);
            addPermissionIfExists(staffPermissions, permissionMap, PermissionConstants.ORDER_UPDATE);
            addPermissionIfExists(staffPermissions, permissionMap, PermissionConstants.ORDER_APPROVE);
            addPermissionIfExists(staffPermissions, permissionMap, PermissionConstants.ORDER_CANCEL);
            addPermissionIfExists(staffPermissions, permissionMap, PermissionConstants.USER_VIEW);
            addPermissionIfExists(staffPermissions, permissionMap, PermissionConstants.PRODUCT_VIEW);
            addPermissionIfExists(staffPermissions, permissionMap, PermissionConstants.ANALYTICS_VIEW);

            staff.setPermissions(staffPermissions);
            roleRepository.save(staff);
            log.info("Assigned {} permissions to ROLE_STAFF", staffPermissions.size());
        } else {
            log.debug("ROLE_STAFF already has permissions assigned");
        }
    }

    private void assignSellerPermissions(Map<String, Permission> permissionMap) {
        Role seller = roleRepository.findByName(RoleConstants.ROLE_SELLER)
                .orElseThrow(() -> new RuntimeException("ROLE_SELLER not found"));

        if (seller.getPermissions().isEmpty()) {
            Set<Permission> sellerPermissions = new HashSet<>();
            addPermissionIfExists(sellerPermissions, permissionMap, PermissionConstants.PRODUCT_VIEW);
            addPermissionIfExists(sellerPermissions, permissionMap, PermissionConstants.PRODUCT_CREATE);
            addPermissionIfExists(sellerPermissions, permissionMap, PermissionConstants.PRODUCT_UPDATE);
            addPermissionIfExists(sellerPermissions, permissionMap, PermissionConstants.PRODUCT_DELETE);
            addPermissionIfExists(sellerPermissions, permissionMap, PermissionConstants.ORDER_VIEW);
            addPermissionIfExists(sellerPermissions, permissionMap, PermissionConstants.REVIEW_VIEW);
            addPermissionIfExists(sellerPermissions, permissionMap, PermissionConstants.RECOMMENDATION_VIEW);

            seller.setPermissions(sellerPermissions);
            roleRepository.save(seller);
            log.info("Assigned {} permissions to ROLE_SELLER", sellerPermissions.size());
        } else {
            log.debug("ROLE_SELLER already has permissions assigned");
        }
    }

    private void assignCustomerPermissions(Map<String, Permission> permissionMap) {
        Role customer = roleRepository.findByName(RoleConstants.ROLE_CUSTOMER)
                .orElseThrow(() -> new RuntimeException("ROLE_CUSTOMER not found"));

        if (customer.getPermissions().isEmpty()) {
            Set<Permission> customerPermissions = new HashSet<>();
            addPermissionIfExists(customerPermissions, permissionMap, PermissionConstants.PRODUCT_VIEW);
            addPermissionIfExists(customerPermissions, permissionMap, PermissionConstants.ORDER_VIEW);
            addPermissionIfExists(customerPermissions, permissionMap, PermissionConstants.REVIEW_CREATE);
            addPermissionIfExists(customerPermissions, permissionMap, PermissionConstants.REVIEW_VIEW);
            addPermissionIfExists(customerPermissions, permissionMap, PermissionConstants.RECOMMENDATION_VIEW);

            customer.setPermissions(customerPermissions);
            roleRepository.save(customer);
            log.info("Assigned {} permissions to ROLE_CUSTOMER", customerPermissions.size());
        } else {
            log.debug("ROLE_CUSTOMER already has permissions assigned");
        }
    }

    private void addPermissionIfExists(Set<Permission> permissions, Map<String, Permission> permissionMap, String permissionName) {
        Permission permission = permissionMap.get(permissionName);
        if (permission != null) {
            permissions.add(permission);
        } else {
            log.warn("Permission not found: {}", permissionName);
        }
    }
}