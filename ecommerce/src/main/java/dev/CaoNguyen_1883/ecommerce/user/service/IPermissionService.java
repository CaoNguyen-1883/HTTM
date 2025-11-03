package dev.CaoNguyen_1883.ecommerce.user.service;

import dev.CaoNguyen_1883.ecommerce.user.dto.PermissionDTO;
import dev.CaoNguyen_1883.ecommerce.user.dto.PermissionRequest;
import dev.CaoNguyen_1883.ecommerce.user.entity.Permission;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface IPermissionService  {
    /**
     * Get all active permissions
     */
    List<PermissionDTO> getAllPermissions();

    /**
     * Get permission by ID
     */
    PermissionDTO getPermissionById(UUID id);

    /**
     * Get permission by name
     */
    PermissionDTO getPermissionByName(String name);

    /**
     * Create new permission
     */
    PermissionDTO createPermission(PermissionRequest request);

    /**
     * Update permission
     */
    PermissionDTO updatePermission(UUID id, PermissionRequest request);

    /**
     * Soft delete permission
     */
    void deletePermission(UUID id);

    /**
     * Restore soft-deleted permission
     */
    PermissionDTO restorePermission(UUID id);

    /**
     * Get permissions by IDs (internal use for Role service)
     */
    Set<Permission> getPermissionEntitiesByIds(Set<UUID> ids);

    /**
     * Check if permission exists by ID
     */
    boolean existsById(UUID id);

    /**
     * Check if permission name exists
     */
    boolean existsByName(String name);
}
