package dev.CaoNguyen_1883.ecommerce.user.service;

import dev.CaoNguyen_1883.ecommerce.user.dto.PermissionDTO;
import dev.CaoNguyen_1883.ecommerce.user.dto.RoleDTO;
import dev.CaoNguyen_1883.ecommerce.user.dto.RoleRequest;
import dev.CaoNguyen_1883.ecommerce.user.entity.Role;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface IRoleService {
    /**
     * Get all active roles
     */
    List<RoleDTO> getAllRoles();

    /**
     * Get role by ID with permissions
     */
    RoleDTO getRoleById(UUID id);

    /**
     * Get role by name
     */
    RoleDTO getRoleByName(String name);

    /**
     * Create new role with permissions
     */
    RoleDTO createRole(RoleRequest request);

    /**
     * Update role
     */
    RoleDTO updateRole(UUID id, RoleRequest request);

    /**
     * Assign permissions to role
     */
    RoleDTO assignPermissions(UUID roleId, Set<UUID> permissionIds);

    /**
     * Remove permissions from role
     */
    RoleDTO removePermissions(UUID roleId, Set<UUID> permissionIds);

    /**
     * Soft delete role
     */
    void deleteRole(UUID id);

    /**
     * Restore soft-deleted role
     */
    RoleDTO restoreRole(UUID id);

    /**
     * Get roles by IDs (internal use for User service)
     */
    Set<Role> getRoleEntitiesByIds(Set<UUID> ids);

    /**
     * Check if role exists
     */
    boolean existsById(UUID id);

    /**
     * Check if role name exists
     */
    boolean existsByName(String name);
}
