package dev.CaoNguyen_1883.ecommerce.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrandRequest {
    @NotBlank(message = "Brand name is required")
    @Size(max = 100)
    @Schema(description = "Brand name", example = "Logitech")
    private String name;

    @Size(max = 500)
    @Schema(description = "Brand description")
    private String description;

    @Schema(description = "Brand logo URL")
    private String logoUrl;

    @Schema(description = "Brand website")
    private String website;

    @Size(max = 50)
    @Schema(description = "Country of origin", example = "Switzerland")
    private String countryOfOrigin;

    @Schema(description = "Featured brand for homepage")
    private Boolean isFeatured;
}