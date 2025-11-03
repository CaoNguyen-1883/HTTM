package dev.CaoNguyen_1883.ecommerce.product.dto;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariantRequest {
    @NotBlank(message = "SKU is required")
    @Size(max = 100)
    @Schema(description = "Stock Keeping Unit", example = "LG-GPW-BLK-001")
    private String sku;

    @NotBlank(message = "Variant name is required")
    @Size(max = 100)
    @Schema(description = "Variant name", example = "Black")
    private String name;

    @DecimalMin(value = "0.0", message = "Price must be greater than 0")
    @Schema(description = "Variant price (optional, inherits from product if not set)")
    private BigDecimal price;

    @Min(value = 0, message = "Stock cannot be negative")
    @NotNull(message = "Stock is required")
    @Schema(description = "Available stock quantity", example = "50")
    private Integer stock;

    @Schema(description = "Variant specifications as JSON", example = "{\"dpi\": 16000, \"weight\": 80}")
    private JsonNode specifications;

    @Schema(description = "Variant attributes as JSON", example = "{\"color\": \"Black\"}")
    private JsonNode attributes;

    @Schema(description = "Set as default variant")
    private Boolean isDefault;

    @Schema(description = "Display order")
    private Integer displayOrder;
}
