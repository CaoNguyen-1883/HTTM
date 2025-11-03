package dev.CaoNguyen_1883.ecommerce.product.dto;

import dev.CaoNguyen_1883.ecommerce.product.entity.ProductStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDto {
    private UUID id;
    private String name;
    private String slug;
    private String description;
    private String shortDescription;
    private CategoryDto category;
    private BrandDto brand;
    private UUID sellerId;
    private String sellerName;
    private BigDecimal basePrice;
    private ProductStatus status;
    private List<String> tags;
    private Integer viewCount;
    private Integer purchaseCount;
    private BigDecimal averageRating;
    private Integer totalReviews;
    private Integer totalStock;
    private List<ProductVariantDto> variants;
    private List<ProductImageDto> images;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
}
