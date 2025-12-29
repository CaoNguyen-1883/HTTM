package dev.CaoNguyen_1883.ecommerce.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImageRequest {

    @NotBlank(message = "Image URL is required")
    @Schema(description = "Image URL from MinIO upload", example = "http://localhost:9000/ecommerce-images/products/uuid.jpg")
    private String imageUrl;

    @Schema(description = "Alt text for SEO and accessibility", example = "Logitech G Pro Wireless Black")
    private String altText;

    @Schema(description = "Set as primary/main image", example = "true")
    @Builder.Default
    private Boolean isPrimary = false;

    @Schema(description = "Display order (0 is first)", example = "0")
    @Builder.Default
    private Integer displayOrder = 0;
}
