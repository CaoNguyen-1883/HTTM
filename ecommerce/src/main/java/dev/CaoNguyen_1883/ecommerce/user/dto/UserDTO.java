package dev.CaoNguyen_1883.ecommerce.user.dto;

import dev.CaoNguyen_1883.ecommerce.user.entity.AuthProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private UUID id;
    private String email;
    private String fullName;
    private String avatarUrl;
    private AuthProvider provider;
    private Set<RoleDTO> roles;
    private String phone;
    private boolean emailVerified;
    private LocalDateTime createdAt;
}
