package dev.CaoNguyen_1883.ecommerce.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "User creation/update request")
public class UserRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Schema(description = "User email address", example = "user@example.com")
    private String email;

    @Size(max = 100, message = "Full name must not exceed 100 characters")
    @Schema(description = "User full name", example = "John Doe")
    private String fullName;

    @Size(min = 8, message = "Password must be at least 8 characters")
    @Schema(description = "User password (min 8 characters)", example = "password123")
    private String password;

    @Schema(description = "Avatar URL", example = "https://example.com/avatar.jpg")
    private String avatarUrl;

    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone number must be 10-15 digits")
    @Schema(description = "Phone number (10-15 digits)", example = "0123456789")
    private String phone;

    @Schema(description = "Set of role IDs to assign to user")
    private Set<UUID> roleIds;
}