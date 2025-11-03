package dev.CaoNguyen_1883.ecommerce.user.controller;

import dev.CaoNguyen_1883.ecommerce.common.response.ApiResponse;

import dev.CaoNguyen_1883.ecommerce.user.dto.RoleDTO;
import dev.CaoNguyen_1883.ecommerce.user.dto.RoleRequest;
import dev.CaoNguyen_1883.ecommerce.user.service.IRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Tag(name = "Role Management", description = "APIs for managing roles and their permissions")
@SecurityRequirement(name = "Bearer Authentication")
public class RoleController {

    private final IRoleService roleService;

    @Operation(
            summary = "Get all roles",
            description = "Retrieve a list of all active roles with their assigned permissions"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved roles",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing authentication token"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - User does not have ADMIN role"
            )
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<RoleDTO>>> getAllRoles() {
        List<RoleDTO> roles = roleService.getAllRoles();
        return ResponseEntity.ok(ApiResponse.success("Roles retrieved successfully", roles));
    }

    @Operation(
            summary = "Get role by ID",
            description = "Retrieve a specific role by its unique identifier with all permissions"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Role found successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Role not found with the given ID"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden"
            )
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RoleDTO>> getRoleById(
            @Parameter(description = "UUID of the role to retrieve", required = true)
            @PathVariable UUID id) {
        RoleDTO role = roleService.getRoleById(id);
        return ResponseEntity.ok(ApiResponse.success("Role retrieved successfully", role));
    }

    @Operation(
            summary = "Create new role",
            description = "Create a new role with optional permissions. Role name must be unique."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Role created successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Bad request - Invalid input or some permission IDs are invalid"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Conflict - Role with the same name already exists"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden"
            )
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RoleDTO>> createRole(
            @Parameter(description = "Role data to create", required = true)
            @Valid @RequestBody RoleRequest request) {
        RoleDTO created = roleService.createRole(request);
        return ResponseEntity.status(201)
                .body(ApiResponse.created("Role created successfully", created));
    }

    @Operation(
            summary = "Update role",
            description = "Update an existing role including its name, description, and permissions"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Role updated successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Bad request - Invalid input"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Role not found"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Conflict - Role name already exists"
            )
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RoleDTO>> updateRole(
            @Parameter(description = "UUID of the role to update", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Updated role data", required = true)
            @Valid @RequestBody RoleRequest request) {
        RoleDTO updated = roleService.updateRole(id, request);
        return ResponseEntity.ok(ApiResponse.success("Role updated successfully", updated));
    }

    @Operation(
            summary = "Assign permissions to role",
            description = "Add new permissions to an existing role without removing current permissions"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Permissions assigned successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid permission IDs"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Role not found"
            )
    })
    @PostMapping("/{id}/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RoleDTO>> assignPermissions(
            @Parameter(description = "UUID of the role", required = true)
            @PathVariable("id") UUID roleId,
            @Parameter(description = "Set of permission IDs to assign", required = true)
            @RequestBody Set<UUID> permissionIds) {
        RoleDTO updated = roleService.assignPermissions(roleId, permissionIds);
        return ResponseEntity.ok(ApiResponse.success("Permissions assigned successfully", updated));
    }

    @Operation(
            summary = "Remove permissions from role",
            description = "Remove specific permissions from a role"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Permissions removed successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Role not found"
            )
    })
    @DeleteMapping("/{id}/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RoleDTO>> removePermissions(
            @Parameter(description = "UUID of the role", required = true)
            @PathVariable("id") UUID roleId,
            @Parameter(description = "Set of permission IDs to remove", required = true)
            @RequestBody Set<UUID> permissionIds) {
        RoleDTO updated = roleService.removePermissions(roleId, permissionIds);
        return ResponseEntity.ok(ApiResponse.success("Permissions removed successfully", updated));
    }

    @Operation(
            summary = "Delete role (soft delete)",
            description = "Soft delete a role by setting its isActive flag to false"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Role deleted successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Role not found"
            )
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteRole(
            @Parameter(description = "UUID of the role to delete", required = true)
            @PathVariable UUID id) {
        roleService.deleteRole(id);
        return ResponseEntity.ok(ApiResponse.success("Role deleted successfully", null));
    }

    @Operation(
            summary = "Restore deleted role",
            description = "Restore a soft-deleted role"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Role restored successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Role not found"
            )
    })
    @PatchMapping("/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RoleDTO>> restoreRole(
            @Parameter(description = "UUID of the role to restore", required = true)
            @PathVariable UUID id) {
        RoleDTO restored = roleService.restoreRole(id);
        return ResponseEntity.ok(ApiResponse.success("Role restored successfully", restored));
    }
}