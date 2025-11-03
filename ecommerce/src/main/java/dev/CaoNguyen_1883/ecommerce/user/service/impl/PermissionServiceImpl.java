package dev.CaoNguyen_1883.ecommerce.user.service.impl;

import dev.CaoNguyen_1883.ecommerce.common.exception.DuplicateResourceException;
import dev.CaoNguyen_1883.ecommerce.common.exception.ResourceNotFoundException;
import dev.CaoNguyen_1883.ecommerce.user.dto.PermissionDTO;
import dev.CaoNguyen_1883.ecommerce.user.dto.PermissionRequest;
import dev.CaoNguyen_1883.ecommerce.user.entity.Permission;
import dev.CaoNguyen_1883.ecommerce.user.mapper.PermissionMapper;
import dev.CaoNguyen_1883.ecommerce.user.repository.PermissionRepository;
import dev.CaoNguyen_1883.ecommerce.user.service.IPermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionServiceImpl implements IPermissionService {
    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    @Override
    @Cacheable(value = "permissions", key = "'all'")
    public List<PermissionDTO> getAllPermissions() {
        log.debug("Fetching all active permissions");
        return permissionRepository.findAllActive().stream()
                .map(permissionMapper::toDTO)
                .toList();
    }

    @Override
    @Cacheable(value = "permissions", key = "#id")
    public PermissionDTO getPermissionById(UUID id) {
        log.debug("Fetching permission by id {}", id);

        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", id));

        return permissionMapper.toDTO(permission);
    }

    @Override
    @Cacheable(value = "permissions", key = "'name_' + #name")
    public PermissionDTO getPermissionByName(String name) {
        log.debug("Fetching permission by name {}", name);

        Permission permission = permissionRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "name", name));

        return permissionMapper.toDTO(permission);
    }

    @Override
    @CacheEvict(value = "permissions", allEntries = true)
    public PermissionDTO createPermission(PermissionRequest request) {
        log.info("Creating new permission {}", request.getName());

        if(permissionRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Permission", "name", request.getName());
        }

        Permission permission = permissionMapper.toEntity(request);
        Permission savedPermission = permissionRepository.save(permission);

        return permissionMapper.toDTO(savedPermission);
    }

    @Override
    @CacheEvict(value = "permissions", allEntries = true)
    public PermissionDTO updatePermission(UUID id, PermissionRequest request) {
        log.info("Updating permission with id {}", id);

        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", id));

        if(!permission.getName().equals(request.getName())
            && permissionRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Permission", "name", request.getName());
        }

        permissionMapper.updateEntityFromRequest(request, permission);
        Permission savedPermission = permissionRepository.save(permission);

        log.info("Permission updated successfully");

        return permissionMapper.toDTO(savedPermission);
    }

    @Override
    @CacheEvict(value = "permissions", allEntries = true)
    public void deletePermission(UUID id) {
        log.info("Soft deleting permission id: {}", id);

        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", id));

        permission.setIsActive(false);
        permissionRepository.save(permission);

        log.info("Permission soft deleted successfully: {}", id);
    }

    @Override
    @CacheEvict(value = "permissions", allEntries = true)
    public PermissionDTO restorePermission(UUID id) {
        log.info("Restoring permission id: {}", id);

        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", id));

        permission.setIsActive(true);
        Permission restored = permissionRepository.save(permission);

        log.info("Permission restored successfully: {}", id);
        return permissionMapper.toDTO(restored);
    }

    @Override
    public Set<Permission> getPermissionEntitiesByIds(Set<UUID> ids) {
        log.debug("Fetching permissions by ids: {}", ids);
        return permissionRepository.findAllByIdInAndActive(ids);
    }

    @Override
    public boolean existsById(UUID id) {
        return permissionRepository.existsById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return permissionRepository.existsByName(name);
    }
}
