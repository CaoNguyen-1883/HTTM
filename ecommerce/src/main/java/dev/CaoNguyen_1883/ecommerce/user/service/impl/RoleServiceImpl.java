package dev.CaoNguyen_1883.ecommerce.user.service.impl;

import dev.CaoNguyen_1883.ecommerce.common.exception.BadRequestException;
import dev.CaoNguyen_1883.ecommerce.common.exception.DuplicateResourceException;
import dev.CaoNguyen_1883.ecommerce.common.exception.ResourceNotFoundException;
import dev.CaoNguyen_1883.ecommerce.user.dto.RoleDTO;
import dev.CaoNguyen_1883.ecommerce.user.dto.RoleRequest;
import dev.CaoNguyen_1883.ecommerce.user.entity.Permission;
import dev.CaoNguyen_1883.ecommerce.user.entity.Role;
import dev.CaoNguyen_1883.ecommerce.user.mapper.RoleMapper;
import dev.CaoNguyen_1883.ecommerce.user.repository.RoleRepository;
import dev.CaoNguyen_1883.ecommerce.user.service.IPermissionService;
import dev.CaoNguyen_1883.ecommerce.user.service.IRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;


@Service
@Slf4j
@RequiredArgsConstructor
public class RoleServiceImpl implements IRoleService {
    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final IPermissionService permissionService;

    @Override
    @Cacheable(value = "roles", key = "'all'")
    public List<RoleDTO> getAllRoles() {
        log.debug("Fetching all active roles");
        return roleRepository.findAllActive().stream()
                .map(roleMapper::toDTO)
                .toList();
    }

    @Override
    @Cacheable(value = "roles", key = "#id")
    public RoleDTO getRoleById(UUID id) {
        log.debug("Fetching role by id: {}", id);
        Role role = roleRepository.findByIdWithPermissions(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));
        return roleMapper.toDTO(role);
    }

    @Override
    @Cacheable(value = "roles", key = "'name_' + #name")
    public RoleDTO getRoleByName(String name) {
        log.debug("Fetching role by name: {}", name);
        Role role = roleRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", name));
        return roleMapper.toDTO(role);
    }

    @Override
    @CacheEvict(value = {"roles", "users"}, allEntries = true)
    public RoleDTO createRole(RoleRequest request) {
        log.info("Creating new role: {}", request);

        if(roleRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Role", "name", request.getName());
        }

        Role role = roleMapper.toEntity(request);

        if(request.getPermissionIds() != null
            && !request.getPermissionIds().isEmpty()) {
            Set<Permission> permissions = permissionService.getPermissionEntitiesByIds(request.getPermissionIds());
            if(permissions.size() != request.getPermissionIds().size()) {
                throw new BadRequestException("Some permission IDs are invalid or inactive");
            }
            role.setPermissions(permissions);
        }

        Role savedRole = roleRepository.save(role);

        log.info("Role created successfully with id: {}", savedRole.getId());

        return roleMapper.toDTO(savedRole);
    }

    @Override
    @CacheEvict(value = {"roles", "users"}, allEntries = true)
    public RoleDTO updateRole(UUID id, RoleRequest request) {
        log.info("Updating role id: {}", id);

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));

        if (!role.getName().equals(request.getName())
                && roleRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Role", "name", request.getName());
        }

        roleMapper.updateEntityFromRequest(request, role);

        // Update permissions if provided
        if (request.getPermissionIds() != null) {
            if (request.getPermissionIds().isEmpty()) {
                role.getPermissions().clear();
            } else {
                Set<Permission> permissions = permissionService.getPermissionEntitiesByIds(request.getPermissionIds());
                if (permissions.size() != request.getPermissionIds().size()) {
                    throw new BadRequestException("Some permission IDs are invalid or inactive");
                }
                role.setPermissions(permissions);
            }
        }

        Role updated = roleRepository.save(role);

        log.info("Role updated successfully: {}", updated.getId());
        return roleMapper.toDTO(updated);
    }

    @Override
    @CacheEvict(value = {"roles", "users"}, allEntries = true)
    public RoleDTO assignPermissions(UUID roleId, Set<UUID> permissionIds) {
        log.info("Assigning permissions to role id: {}", roleId);

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));

        Set<Permission> permissions = permissionService.getPermissionEntitiesByIds(permissionIds);
        if (permissions.size() != permissionIds.size()) {
            throw new BadRequestException("Some permission IDs are invalid or inactive");
        }

        role.getPermissions().addAll(permissions);
        Role updated = roleRepository.save(role);

        log.info("Permissions assigned successfully to role: {}", roleId);
        return roleMapper.toDTO(updated);
    }

    @Override
    @CacheEvict(value = {"roles", "users"}, allEntries = true)
    public RoleDTO removePermissions(UUID roleId, Set<UUID> permissionIds) {
        log.info("Removing permissions from role id: {}", roleId);

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));

        Set<Permission> permissionsToRemove = permissionService.getPermissionEntitiesByIds(permissionIds);
        role.getPermissions().removeAll(permissionsToRemove);

        Role updated = roleRepository.save(role);

        log.info("Permissions removed successfully from role: {}", roleId);
        return roleMapper.toDTO(updated);
    }

    @Override
    @CacheEvict(value = {"roles", "users"}, allEntries = true)
    public void deleteRole(UUID id) {
        log.info("Soft deleting role id: {}", id);

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));

        role.setIsActive(false);
        roleRepository.save(role);

        log.info("Role soft deleted successfully: {}", id);
    }

    @Override
    @CacheEvict(value = {"roles", "users"}, allEntries = true)
    public RoleDTO restoreRole(UUID id) {
        log.info("Restoring role id: {}", id);

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));

        role.setIsActive(true);
        Role restored = roleRepository.save(role);

        log.info("Role restored successfully: {}", id);
        return roleMapper.toDTO(restored);
    }

    @Override
    public Set<Role> getRoleEntitiesByIds(Set<UUID> ids) {
        log.debug("Fetching roles by ids: {}", ids);
        return roleRepository.findAllByIdInAndActive(ids);
    }

    @Override
    public boolean existsById(UUID id) {
        return roleRepository.existsById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return roleRepository.existsByName(name);
    }
}
