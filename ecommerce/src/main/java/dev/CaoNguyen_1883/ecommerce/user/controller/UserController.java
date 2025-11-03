package dev.CaoNguyen_1883.ecommerce.user.controller;



import dev.CaoNguyen_1883.ecommerce.common.response.ApiResponse;
import dev.CaoNguyen_1883.ecommerce.user.dto.*;
import dev.CaoNguyen_1883.ecommerce.user.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "APIs for managing users, roles assignment, and user profiles")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final IUserService userService;

    @Operation(
            summary = "Get all users",
            description = "Retrieve a paginated list of all active users in the system"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved users",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Requires ADMIN role"
            )
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserSummaryDTO>>> getAllUsers(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<UserSummaryDTO> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
    }

    @Operation(
            summary = "Search users",
            description = "Search users by email or full name with pagination"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Search completed successfully"
            )
    })
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserSummaryDTO>>> searchUsers(
            @Parameter(description = "Search keyword (email or name)", required = true)
            @RequestParam String keyword,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<UserSummaryDTO> users = userService.searchUsers(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success("Search completed successfully", users));
    }

    @Operation(
            summary = "Get users by role",
            description = "Retrieve users filtered by role name with pagination"
    )
    @GetMapping("/role/{roleName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserSummaryDTO>>> getUsersByRole(
            @Parameter(description = "Role name to filter users", required = true)
            @PathVariable String roleName,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<UserSummaryDTO> users = userService.getUsersByRole(roleName, pageable);
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
    }

    @Operation(
            summary = "Get user by ID",
            description = "Retrieve detailed user information including roles"
    )
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(
            @Parameter(description = "User UUID", required = true)
            @PathVariable UUID id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", user));
    }

    @Operation(
            summary = "Create new user",
            description = "Create a new user with optional role assignment"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "User created successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Email already exists"
            )
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDTO>> createUser(
            @Valid @RequestBody UserRequest request) {
        UserDTO created = userService.createUser(request);
        return ResponseEntity.status(201)
                .body(ApiResponse.created("User created successfully", created));
    }

    @Operation(
            summary = "Update user",
            description = "Update user information. All fields are optional."
    )
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UserUpdateRequest request) {
        UserDTO updated = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", updated));
    }

    @Operation(
            summary = "Assign roles to user",
            description = "Add new roles to a user without removing existing roles"
    )
    @PostMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDTO>> assignRoles(
            @PathVariable("id") UUID userId,
            @RequestBody Set<UUID> roleIds) {
        UserDTO updated = userService.assignRoles(userId, roleIds);
        return ResponseEntity.ok(ApiResponse.success("Roles assigned successfully", updated));
    }

    @Operation(
            summary = "Remove roles from user",
            description = "Remove specific roles from a user"
    )
    @DeleteMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDTO>> removeRoles(
            @PathVariable("id") UUID userId,
            @RequestBody Set<UUID> roleIds) {
        UserDTO updated = userService.removeRoles(userId, roleIds);
        return ResponseEntity.ok(ApiResponse.success("Roles removed successfully", updated));
    }

    @Operation(
            summary = "Change password",
            description = "Change user password by providing current and new password"
    )
    @PostMapping("/{id}/change-password")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @PathVariable UUID id,
            @Valid @RequestBody PasswordChangeRequest request) {
        userService.changePassword(id, request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
    }

    @Operation(
            summary = "Verify email",
            description = "Mark user email as verified"
    )
    @PostMapping("/{id}/verify-email")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@PathVariable UUID id) {
        userService.verifyEmail(id);
        return ResponseEntity.ok(ApiResponse.success("Email verified successfully", null));
    }

    @Operation(
            summary = "Delete user",
            description = "Soft delete a user (sets isActive to false)"
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
    }

    @Operation(
            summary = "Restore user",
            description = "Restore a soft-deleted user"
    )
    @PatchMapping("/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDTO>> restoreUser(@PathVariable UUID id) {
        UserDTO restored = userService.restoreUser(id);
        return ResponseEntity.ok(ApiResponse.success("User restored successfully", restored));
    }
}