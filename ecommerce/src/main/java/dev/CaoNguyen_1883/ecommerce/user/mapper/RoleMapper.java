package dev.CaoNguyen_1883.ecommerce.user.mapper;

import dev.CaoNguyen_1883.ecommerce.user.dto.RoleDTO;
import dev.CaoNguyen_1883.ecommerce.user.dto.RoleRequest;
import dev.CaoNguyen_1883.ecommerce.user.entity.Role;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring", uses = {PermissionMapper.class})
public interface RoleMapper {
    RoleDTO toDTO(Role role);

    // Entity List -> DTO List
    List<RoleDTO> toDTOList(List<Role> roles);

    // Entity Set -> DTO Set
    Set<RoleDTO> toDTOSet(Set<Role> roles);

    // Request -> Entity (without permissions, set manually in service)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    Role toEntity(RoleRequest request);

    // Update existing entity from request
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    void updateEntityFromRequest(RoleRequest request, @MappingTarget Role role);
}
