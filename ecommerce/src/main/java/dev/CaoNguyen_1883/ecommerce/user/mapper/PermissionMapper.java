package dev.CaoNguyen_1883.ecommerce.user.mapper;


import dev.CaoNguyen_1883.ecommerce.user.dto.PermissionDTO;
import dev.CaoNguyen_1883.ecommerce.user.dto.PermissionRequest;
import dev.CaoNguyen_1883.ecommerce.user.entity.Permission;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    // Entity -> DTO
    PermissionDTO toDTO(Permission permission);

    // Entity List -> DTO List
    List<PermissionDTO> toDTOList(List<Permission> permissions);

    // Entity Set -> DTO Set
    Set<PermissionDTO> toDTOSet(Set<Permission> permissions);

    // Request -> Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    Permission toEntity(PermissionRequest request);

    // Update existing entity from request
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    void updateEntityFromRequest(PermissionRequest request, @MappingTarget Permission permission);
}