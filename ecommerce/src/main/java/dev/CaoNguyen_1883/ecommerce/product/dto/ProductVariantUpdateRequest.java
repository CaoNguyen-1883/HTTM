package dev.CaoNguyen_1883.ecommerce.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariantUpdateRequest {
    @NotNull(message = "Variant ID is required")
    @Schema(description = "Variant ID to update", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID variantId;

    @Schema(description = "Variant-specific image URLs (replaces all existing variant images if provided)")
    private List<String> imageUrls;
}
