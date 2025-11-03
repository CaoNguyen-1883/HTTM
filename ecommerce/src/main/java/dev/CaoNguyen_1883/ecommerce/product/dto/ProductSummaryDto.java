package dev.CaoNguyen_1883.ecommerce.product.dto;

import dev.CaoNguyen_1883.ecommerce.product.entity.ProductStatus;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSummaryDto {
    private UUID id;
    private String name;
    private String slug;
    private String shortDescription;
    private String categoryName;
    private String brandName;
    private BigDecimal basePrice;
    private BigDecimal minPrice;  // Minimum price from variants
    private BigDecimal maxPrice;  // Maximum price from variants
    private ProductStatus status;
    private String primaryImage;
    private BigDecimal averageRating;
    private Integer totalReviews;
    private Integer totalStock;
    private Boolean hasStock;
    private UUID defaultVariantId;  // First available variant ID for quick add to cart
}