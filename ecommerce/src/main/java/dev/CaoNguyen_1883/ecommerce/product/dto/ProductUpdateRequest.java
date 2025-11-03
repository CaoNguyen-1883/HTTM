package dev.CaoNguyen_1883.ecommerce.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductUpdateRequest {
    @Size(max = 200)
    @Schema(description = "Product name")
    private String name;

    @Schema(description = "Product description")
    private String description;

    @Size(max = 500)
    @Schema(description = "Short description")
    private String shortDescription;

    @Schema(description = "Category ID")
    private UUID categoryId;

    @Schema(description = "Brand ID")
    private UUID brandId;

    @DecimalMin(value = "0.0")
    @Schema(description = "Base price")
    private BigDecimal basePrice;

    @Schema(description = "Product tags")
    private List<String> tags;
}
