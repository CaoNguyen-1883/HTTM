package dev.CaoNguyen_1883.ecommerce.user.mapper;

import dev.CaoNguyen_1883.ecommerce.user.dto.UserDTO;
import dev.CaoNguyen_1883.ecommerce.user.dto.UserRequest;
import dev.CaoNguyen_1883.ecommerce.user.dto.UserSummaryDTO;
import dev.CaoNguyen_1883.ecommerce.user.dto.UserUpdateRequest;
import dev.CaoNguyen_1883.ecommerce.user.entity.Role;
import dev.CaoNguyen_1883.ecommerce.user.entity.User;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Mapper(componentModel = "spring", uses = {RoleMapper.class})
public interface UserMapper {

    // Entity -> DTO (with nested roles)
    UserDTO toDTO(User user);

    // Entity List -> DTO List
    List<UserDTO> toDTOList(List<User> users);

    // Entity -> Summary DTO (without nested objects)
    @Mapping(target = "roleNames", expression = "java(mapRoleNames(user.getRoles()))")
    UserSummaryDTO toSummaryDTO(User user);

    // Entity List -> Summary DTO List
    List<UserSummaryDTO> toSummaryDTOList(List<User> users);

    // Request -> Entity (without roles and password, set manually in service)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "provider", ignore = true)
    @Mapping(target = "providerId", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    User toEntity(UserRequest request);

    // Update existing entity from request
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "provider", ignore = true)
    @Mapping(target = "providerId", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    void updateEntityFromRequest(UserUpdateRequest request, @MappingTarget User user);

    // Helper method to map role names
    default Set<String> mapRoleNames(Set<Role> roles) {
        if (roles == null) {
            return Set.of();
        }
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }
}