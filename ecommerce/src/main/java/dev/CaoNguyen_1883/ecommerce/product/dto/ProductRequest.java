package dev.CaoNguyen_1883.ecommerce.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequest {
    @NotBlank(message = "Product name is required")
    @Size(max = 200)
    @Schema(description = "Product name", example = "Logitech G Pro Wireless")
    private String name;

    @NotBlank(message = "Description is required")
    @Schema(description = "Full product description")
    private String description;

    @Size(max = 500)
    @Schema(description = "Short description for listing")
    private String shortDescription;

    @NotNull(message = "Category is required")
    @Schema(description = "Category ID")
    private UUID categoryId;

    @NotNull(message = "Brand is required")
    @Schema(description = "Brand ID")
    private UUID brandId;

    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.0", message = "Price must be greater than 0")
    @Schema(description = "Base price", example = "100.00")
    private BigDecimal basePrice;

    @Schema(description = "Product tags for search and recommendation", example = "[\"gaming\", \"wireless\"]")
    private List<String> tags;

    @Schema(description = "Meta title for SEO")
    private String metaTitle;

    @Schema(description = "Meta description for SEO")
    private String metaDescription;

    @Schema(description = "Meta keywords for SEO")
    private String metaKeywords;

    @Valid
    @Schema(description = "Product variants")
    private List<ProductVariantRequest> variants;
}