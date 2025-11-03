package dev.CaoNguyen_1883.ecommerce.user.dto;

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
public class UserSummaryDTO {
    private UUID id;
    private String email;
    private String fullName;
    private String avatarUrl;
    private Set<String> roleNames;
    private boolean emailVerified;
    private LocalDateTime createdAt;
}

