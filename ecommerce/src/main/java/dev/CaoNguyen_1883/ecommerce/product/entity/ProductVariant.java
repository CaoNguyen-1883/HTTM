package dev.CaoNguyen_1883.ecommerce.product.entity;

import dev.CaoNguyen_1883.ecommerce.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "product_variants", indexes = {
        @Index(name = "idx_variant_sku", columnList = "sku"),
        @Index(name = "idx_variant_product", columnList = "product_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ProductVariant extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(unique = true, nullable = false, length = 100)
    private String sku;  // Stock Keeping Unit: "LG-GPW-BLK-001"

    @Column(nullable = false, length = 100)
    private String name;  // "Black", "White", "RGB Edition"

    @Column(precision = 12, scale = 2)
    private BigDecimal price;  // Can override product base price

    @Column(nullable = false)
    @Builder.Default
    private Integer stock = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer reservedStock = 0;  // In carts but not purchased

    // Specifications for this variant (JSON format)
    @Column(columnDefinition = "json")
    private String specifications;  // {"dpi": 16000, "weight": 80, "connectivity": "wireless"}

    // Variant-specific attributes (JSON format)
    @Column(columnDefinition = "json")
    private String attributes;  // {"color": "Black", "size": "Medium", "material": "Plastic"}

    @Column(nullable = false)
    @Builder.Default
    private Boolean isDefault = false;  // Default variant to show

    @Column(nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    // Images specific to this variant
    @OneToMany(mappedBy = "variant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<ProductImage> images = new HashSet<>();

    // Helper methods
    public Integer getAvailableStock() {
        return stock - reservedStock;
    }

    public boolean hasStock() {
        return getAvailableStock() > 0;
    }

    public boolean canReserve(int quantity) {
        return getAvailableStock() >= quantity;
    }

    public void reserveStock(int quantity) {
        if (!canReserve(quantity)) {
            throw new IllegalStateException("Insufficient stock to reserve");
        }
        this.reservedStock += quantity;
    }

    public void releaseReservedStock(int quantity) {
        this.reservedStock = Math.max(0, this.reservedStock - quantity);
    }

    public void decreaseStock(int quantity) {
        if (this.stock < quantity) {
            throw new IllegalStateException("Insufficient stock");
        }
        this.stock -= quantity;
        this.reservedStock = Math.max(0, this.reservedStock - quantity);
    }

    public void increaseStock(int quantity) {
        this.stock += quantity;
    }
}