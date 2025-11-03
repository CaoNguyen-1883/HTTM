package dev.CaoNguyen_1883.ecommerce.user.service.impl;


import dev.CaoNguyen_1883.ecommerce.common.exception.BadRequestException;
import dev.CaoNguyen_1883.ecommerce.common.exception.DuplicateResourceException;
import dev.CaoNguyen_1883.ecommerce.common.exception.ResourceNotFoundException;
import dev.CaoNguyen_1883.ecommerce.user.dto.*;
import dev.CaoNguyen_1883.ecommerce.user.entity.AuthProvider;
import dev.CaoNguyen_1883.ecommerce.user.entity.Role;
import dev.CaoNguyen_1883.ecommerce.user.entity.User;
import dev.CaoNguyen_1883.ecommerce.user.mapper.UserMapper;
import dev.CaoNguyen_1883.ecommerce.user.repository.UserRepository;
import dev.CaoNguyen_1883.ecommerce.user.service.IRoleService;
import dev.CaoNguyen_1883.ecommerce.user.service.IUserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserServiceImpl implements IUserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final IRoleService roleService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Page<UserSummaryDTO> getAllUsers(Pageable pageable) {
        log.debug("Fetching all active users, page: {}", pageable.getPageNumber());
        return userRepository.findAllActive(pageable)
                .map(userMapper::toSummaryDTO);
    }

    @Override
    public Page<UserSummaryDTO> searchUsers(String keyword, Pageable pageable) {
        log.debug("Searching users with keyword: {}", keyword);
        return userRepository.searchUsers(keyword, pageable)
                .map(userMapper::toSummaryDTO);
    }

    @Override
    public Page<UserSummaryDTO> getUsersByRole(String roleName, Pageable pageable) {
        log.debug("Fetching users with role: {}", roleName);
        return userRepository.findByRoleName(roleName, pageable)
                .map(userMapper::toSummaryDTO);
    }

    @Override
    @Cacheable(value = "users", key = "#id")
    public UserDTO getUserById(UUID id) {
        log.debug("Fetching user by id: {}", id);
        User user = userRepository.findByIdWithRoles(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return userMapper.toDTO(user);
    }

    @Override
    @Cacheable(value = "users", key = "'email_' + #email")
    public UserDTO getUserByEmail(String email) {
        log.debug("Fetching user by email: {}", email);
        User user = userRepository.findByEmailWithRoles(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return userMapper.toDTO(user);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public UserDTO createUser(UserRequest request) {
        log.info("Creating new user: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        User user = userMapper.toEntity(request);

        // Set password
        if (request.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        // Set default provider
        user.setProvider(AuthProvider.LOCAL);
        user.setEmailVerified(false);

        // Assign roles if provided
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            Set<Role> roles = roleService.getRoleEntitiesByIds(request.getRoleIds());
            if (roles.size() != request.getRoleIds().size()) {
                throw new BadRequestException("Some role IDs are invalid or inactive");
            }
            user.setRoles(roles);
        }

        User saved = userRepository.save(user);

        log.info("User created successfully with id: {}", saved.getId());
        return userMapper.toDTO(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public UserDTO updateUser(UUID id, UserUpdateRequest request) {
        log.info("Updating user id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // Check email uniqueness if changed
        if (request.getEmail() != null
                && !user.getEmail().equals(request.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        userMapper.updateEntityFromRequest(request, user);

        // Update roles if provided
        if (request.getRoleIds() != null) {
            if (request.getRoleIds().isEmpty()) {
                user.getRoles().clear();
            } else {
                Set<Role> roles = roleService.getRoleEntitiesByIds(request.getRoleIds());
                if (roles.size() != request.getRoleIds().size()) {
                    throw new BadRequestException("Some role IDs are invalid or inactive");
                }
                user.setRoles(roles);
            }
        }

        User updated = userRepository.save(user);

        log.info("User updated successfully: {}", updated.getId());
        return userMapper.toDTO(updated);
    }

    @Override
    @CacheEvict(value = "users", allEntries = true)
    @Transactional
    public UserDTO assignRoles(UUID userId, Set<UUID> roleIds) {
        log.info("Assigning roles to user id: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Set<Role> roles = roleService.getRoleEntitiesByIds(roleIds);
        if (roles.size() != roleIds.size()) {
            throw new BadRequestException("Some role IDs are invalid or inactive");
        }
        roles.forEach(r -> log.info("role: {}", r.getName()));
        user.getRoles().addAll(roles);
        User updated = userRepository.save(user);

        log.info("Roles assigned successfully to user: {}", userId);
        return userMapper.toDTO(updated);
    }

    @Override
    @CacheEvict(value = "users", allEntries = true)
    public UserDTO removeRoles(UUID userId, Set<UUID> roleIds) {
        log.info("Removing roles from user id: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Set<Role> rolesToRemove = roleService.getRoleEntitiesByIds(roleIds);
        user.getRoles().removeAll(rolesToRemove);

        User updated = userRepository.save(user);

        log.info("Roles removed successfully from user: {}", userId);
        return userMapper.toDTO(updated);
    }

    @Override
    @CacheEvict(value = "users", key = "#userId")
    public void changePassword(UUID userId, PasswordChangeRequest request) {        log.info("Changing password for user id: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        // Set new password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Password changed successfully for user: {}", userId);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    public void verifyEmail(UUID userId) {
        log.info("Verifying email for user id: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setEmailVerified(true);
        userRepository.save(user);

        log.info("Email verified successfully for user: {}", userId);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public void deleteUser(UUID id) {
        log.info("Soft deleting user id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        user.setIsActive(false);
        userRepository.save(user);

        log.info("User soft deleted successfully: {}", id);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public UserDTO restoreUser(UUID id) {
        log.info("Restoring user id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        user.setIsActive(true);
        User restored = userRepository.save(user);

        log.info("User restored successfully: {}", id);
        return userMapper.toDTO(restored);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
