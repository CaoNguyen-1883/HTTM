package dev.CaoNguyen_1883.ecommerce.user.service;


import dev.CaoNguyen_1883.ecommerce.user.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;
import java.util.UUID;

public interface IUserService  {
    /**
     * Get all active users with pagination
     */
    Page<UserSummaryDTO> getAllUsers(Pageable pageable);

    /**
     * Search users by keyword with pagination
     */
    Page<UserSummaryDTO> searchUsers(String keyword, Pageable pageable);

    /**
     * Get users by role name with pagination
     */
    Page<UserSummaryDTO> getUsersByRole(String roleName, Pageable pageable);

    /**
     * Get user by ID with roles
     */
    UserDTO getUserById(UUID id);

    /**
     * Get user by email
     */
    UserDTO getUserByEmail(String email);

    /**
     * Create new user
     */
    UserDTO createUser(UserRequest request);

    /**
     * Update user
     */
    UserDTO updateUser(UUID id, UserUpdateRequest request);

    /**
     * Assign roles to user
     */
    UserDTO assignRoles(UUID userId, Set<UUID> roleIds);

    /**
     * Remove roles from user
     */
    UserDTO removeRoles(UUID userId, Set<UUID> roleIds);

    /**
     * Change user password
     */
    void changePassword(UUID userId, PasswordChangeRequest request);

    /**
     * Verify user email
     */
    void verifyEmail(UUID userId);

    /**
     * Soft delete user
     */
    void deleteUser(UUID id);

    /**
     * Restore soft-deleted user
     */
    UserDTO restoreUser(UUID id);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);
}
