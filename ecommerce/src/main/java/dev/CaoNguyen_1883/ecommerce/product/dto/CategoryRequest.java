package dev.CaoNguyen_1883.ecommerce.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryRequest {
    @NotBlank(message = "Category name is required")
    @Size(max = 100)
    @Schema(description = "Category name", example = "Gaming Mouse")
    private String name;

    @Size(max = 500)
    @Schema(description = "Category description")
    private String description;

    @Schema(description = "Parent category ID for sub-category")
    private UUID parentId;

    @Schema(description = "Category image URL")
    private String imageUrl;

    @Schema(description = "Display order for sorting")
    private Integer displayOrder;
}
