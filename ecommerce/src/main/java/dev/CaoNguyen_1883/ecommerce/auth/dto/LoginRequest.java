package dev.CaoNguyen_1883.ecommerce.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Login request")
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Schema(description = "User email", example = "admin@ecommerce.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Schema(description = "User password", example = "Admin@123")
    private String password;

    // NEW: Optional expected role for portal-specific login
    @Schema(description = "Expected role for validation (optional)",
            example = "ROLE_ADMIN",
            allowableValues = {"ROLE_ADMIN", "ROLE_CUSTOMER", "ROLE_SELLER", "ROLE_STAFF"})
    private String expectedRole;
}
