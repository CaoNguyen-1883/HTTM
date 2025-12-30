package dev.CaoNguyen_1883.ecommerce.user.mapper;

import dev.CaoNguyen_1883.ecommerce.user.dto.PermissionDTO;
import dev.CaoNguyen_1883.ecommerce.user.dto.PermissionRequest;
import dev.CaoNguyen_1883.ecommerce.user.entity.Permission;
import org.springframework.stereotype.Component;

@Component
public class PermissionMapper {

    public PermissionDTO toDTO(Permission permission) {
        if (permission == null) {
            return null;
        }

        return PermissionDTO.builder()
            .id(permission.getId())
            .name(permission.getName())
            .description(permission.getDescription())
            .createdAt(permission.getCreatedAt())
            .createdBy(permission.getCreatedBy())
            .build();
    }

    public Permission toEntity(PermissionRequest request) {
        if (request == null) {
            return null;
        }

        return Permission.builder()
            .name(request.getName())
            .description(request.getDescription())
            .isActive(true)
            .build();
    }

    public void updateEntityFromRequest(
        PermissionRequest request,
        Permission permission
    ) {
        if (permission == null || request == null) {
            return;
        }

        if (request.getName() != null) {
            permission.setName(request.getName());
        }
        if (request.getDescription() != null) {
            permission.setDescription(request.getDescription());
        }
    }
}
