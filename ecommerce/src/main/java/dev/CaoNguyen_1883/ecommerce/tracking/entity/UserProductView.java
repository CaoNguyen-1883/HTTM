package dev.CaoNguyen_1883.ecommerce.tracking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity for tracking user views on products
 * Aggregated view: one row per user-product pair
 * Used for ML recommendation system
 */
@Entity
@Table(name = "user_product_views",
       indexes = {
           @Index(name = "idx_user_views", columnList = "user_id, last_viewed_at"),
           @Index(name = "idx_product_viewers", columnList = "product_id, view_count"),
           @Index(name = "idx_last_viewed", columnList = "last_viewed_at"),
           @Index(name = "idx_view_count", columnList = "view_count"),
           @Index(name = "idx_recent_views", columnList = "user_id, last_viewed_at")
       })
@IdClass(UserProductViewId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProductView {

    @Id
    @Column(name = "user_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID userId;

    @Id
    @Column(name = "product_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID productId;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    @Column(name = "last_viewed_at", nullable = false)
    private LocalDateTime lastViewedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Constructor for creating new view record
     */
    public UserProductView(UUID userId, UUID productId) {
        this.userId = userId;
        this.productId = productId;
        this.viewCount = 1;
        this.lastViewedAt = LocalDateTime.now();
    }

    /**
     * Increment view count and update timestamp
     */
    public void incrementView() {
        this.viewCount++;
        this.lastViewedAt = LocalDateTime.now();
    }
}
