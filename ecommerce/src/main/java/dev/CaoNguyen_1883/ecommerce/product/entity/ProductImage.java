package dev.CaoNguyen_1883.ecommerce.product.entity;

import dev.CaoNguyen_1883.ecommerce.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "product_images", indexes = {
        @Index(name = "idx_image_product", columnList = "product_id"),
        @Index(name = "idx_image_variant", columnList = "variant_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ProductImage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private ProductVariant variant;  // Nullable - image can be for product or specific variant

    @Column(nullable = false, length = 500)
    private String imageUrl;  // "/uploads/products/uuid-image.jpg"

    @Column(length = 100)
    private String altText;  // For SEO and accessibility

    @Column(nullable = false)
    @Builder.Default
    private Boolean isPrimary = false;  // Main image for product/variant

    @Column(nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    // Helper methods
    public boolean isProductImage() {
        return variant == null;
    }

    public boolean isVariantImage() {
        return variant != null;
    }
}
