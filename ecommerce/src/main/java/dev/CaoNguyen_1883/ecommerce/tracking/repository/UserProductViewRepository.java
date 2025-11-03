package dev.CaoNguyen_1883.ecommerce.tracking.repository;

import dev.CaoNguyen_1883.ecommerce.tracking.entity.UserProductView;
import dev.CaoNguyen_1883.ecommerce.tracking.entity.UserProductViewId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository for UserProductView entity
 * Handles view tracking for recommendation system
 */
@Repository
public interface UserProductViewRepository extends JpaRepository<UserProductView, UserProductViewId> {

    /**
     * Track a product view using native SQL with ON DUPLICATE KEY UPDATE
     * This is the most efficient way - single query that inserts or updates
     *
     * @param userId User ID (binary format)
     * @param productId Product ID (binary format)
     * @param viewedAt Timestamp of view
     */
    @Modifying
    @Query(value = """
        INSERT INTO user_product_views (user_id, product_id, view_count, last_viewed_at, created_at, updated_at)
        VALUES (:userId, :productId, 1, :viewedAt, :viewedAt, :viewedAt)
        ON DUPLICATE KEY UPDATE
            view_count = view_count + 1,
            last_viewed_at = :viewedAt,
            updated_at = :viewedAt
        """, nativeQuery = true)
    void trackView(@Param("userId") byte[] userId,
                   @Param("productId") byte[] productId,
                   @Param("viewedAt") LocalDateTime viewedAt);

    /**
     * Get user's view history ordered by most recent
     * Used for personalized recommendations
     *
     * @param userId User ID
     * @return List of user's viewed products
     */
    @Query("SELECT upv FROM UserProductView upv WHERE upv.userId = :userId ORDER BY upv.lastViewedAt DESC")
    List<UserProductView> findByUserIdOrderByLastViewedAtDesc(@Param("userId") UUID userId);

    /**
     * Get user's most viewed products
     * Used for understanding user preferences
     *
     * @param userId User ID
     * @return List of user's most viewed products
     */
    @Query("SELECT upv FROM UserProductView upv WHERE upv.userId = :userId ORDER BY upv.viewCount DESC, upv.lastViewedAt DESC")
    List<UserProductView> findByUserIdOrderByViewCountDesc(@Param("userId") UUID userId);

    /**
     * Get products viewed by user in last N days
     * Used for recent interest tracking
     *
     * @param userId User ID
     * @param sinceDate Date to filter from
     * @return List of recently viewed products
     */
    @Query("SELECT upv FROM UserProductView upv WHERE upv.userId = :userId AND upv.lastViewedAt >= :sinceDate ORDER BY upv.lastViewedAt DESC")
    List<UserProductView> findRecentViewsByUser(@Param("userId") UUID userId,
                                                  @Param("sinceDate") LocalDateTime sinceDate);

    /**
     * Get top viewers for a product
     * Used for product analytics
     *
     * @param productId Product ID
     * @return List of top viewers
     */
    @Query("SELECT upv FROM UserProductView upv WHERE upv.productId = :productId ORDER BY upv.viewCount DESC, upv.lastViewedAt DESC")
    List<UserProductView> findTopViewersByProduct(@Param("productId") UUID productId);

    /**
     * Count total views for a product
     *
     * @param productId Product ID
     * @return Total view count
     */
    @Query("SELECT COALESCE(SUM(upv.viewCount), 0) FROM UserProductView upv WHERE upv.productId = :productId")
    Long countTotalViewsByProduct(@Param("productId") UUID productId);

    /**
     * Count unique viewers for a product
     *
     * @param productId Product ID
     * @return Number of unique viewers
     */
    @Query("SELECT COUNT(upv) FROM UserProductView upv WHERE upv.productId = :productId")
    Long countUniqueViewersByProduct(@Param("productId") UUID productId);
}
