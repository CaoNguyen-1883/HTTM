package dev.CaoNguyen_1883.ecommerce.product.entity;

import dev.CaoNguyen_1883.ecommerce.common.entity.BaseEntity;
import dev.CaoNguyen_1883.ecommerce.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Formula;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_product_category", columnList = "category_id"),
        @Index(name = "idx_product_brand", columnList = "brand_id"),
        @Index(name = "idx_product_seller", columnList = "seller_id"),
        @Index(name = "idx_product_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Product extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(unique = true, nullable = false, length = 250)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String shortDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal basePrice;  // Base price, variants can override

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ProductStatus status = ProductStatus.PENDING;

    // Tags for search and recommendation (stored as JSON array)
    @Column(columnDefinition = "json")
    private String tags;  // ["gaming", "wireless", "rgb", "lightweight"]

    // Approval tracking
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    private java.time.LocalDateTime approvedAt;

    private String rejectionReason;

    // Statistics for recommendation system
    @Column(nullable = false)
    @Builder.Default
    private Integer viewCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer purchaseCount = 0;

    @Column(precision = 3, scale = 2)
    private BigDecimal averageRating;  // 0.00 - 5.00

    @Column(nullable = false)
    @Builder.Default
    private Integer totalReviews = 0;

    // SEO
    private String metaTitle;
    private String metaDescription;
    private String metaKeywords;

    // Relationships
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<ProductVariant> variants = new HashSet<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<ProductImage> images = new HashSet<>();

    // Computed fields
    @Formula("(SELECT COALESCE(SUM(pv.stock), 0) FROM product_variants pv WHERE pv.product_id = id)")
    private Integer totalStock;

    // Helper methods
    public void addVariant(ProductVariant variant) {
        variants.add(variant);
        variant.setProduct(this);
    }

    public void removeVariant(ProductVariant variant) {
        variants.remove(variant);
        variant.setProduct(null);
    }

    public void addImage(ProductImage image) {
        images.add(image);
        image.setProduct(this);
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

    public void incrementPurchaseCount() {
        this.purchaseCount++;
    }

    public boolean hasStock() {
        return variants.stream().anyMatch(v -> v.getStock() > 0);
    }

    public void setAverageRating(Long averageRating) {
        this.averageRating = BigDecimal.valueOf(averageRating);
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = BigDecimal.valueOf(averageRating);
    }
}
