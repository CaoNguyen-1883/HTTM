package dev.CaoNguyen_1883.ecommerce.order.entity;

import dev.CaoNguyen_1883.ecommerce.common.entity.BaseEntity;
import dev.CaoNguyen_1883.ecommerce.product.entity.ProductVariant;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items", indexes = {
        @Index(name = "idx_order_item_order", columnList = "order_id"),
        @Index(name = "idx_order_item_variant", columnList = "variant_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class OrderItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")  // Nullable - variant might be deleted
    private ProductVariant variant;

    // Snapshot data - preserve even if product/variant is deleted or modified
    @Column(nullable = false, length = 200)
    private String productName;

    @Column(nullable = false, length = 100)
    private String variantSku;

    @Column(nullable = false, length = 100)
    private String variantName;

    @Column(length = 500)
    private String productImage;  // Primary image URL

    // Product snapshot as JSON for ML/analytics
    @Column(columnDefinition = "json")
    private String productSnapshot;  // {"productId": "...", "categoryId": "...", "brandId": "..."}

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;  // Price at time of order

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;  // price * quantity

    // Helper methods
    public void calculateSubtotal() {
        this.subtotal = price.multiply(BigDecimal.valueOf(quantity));
    }
}