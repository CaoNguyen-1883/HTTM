package dev.CaoNguyen_1883.ecommerce.tracking.service;

import dev.CaoNguyen_1883.ecommerce.tracking.entity.UserProductView;
import dev.CaoNguyen_1883.ecommerce.tracking.repository.UserProductViewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service for tracking product views
 * Uses async processing to avoid blocking main request
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductViewService {

    private final UserProductViewRepository viewRepository;

    /**
     * Track a product view (ASYNC - does not block main request)
     *
     * This method runs in a separate thread pool, so any failures
     * will NOT cause the main product request to fail.
     *
     * @param userId User ID (can be null for guest users)
     * @param productId Product ID
     */
    @Async("viewTrackingExecutor")
    @Transactional
    public void trackView(UUID userId, UUID productId) {
        // Validate inputs
        if (productId == null) {
            log.warn("Cannot track view: product ID is null");
            return;
        }

        // Skip guest users (or implement session-based tracking)
        if (userId == null) {
            log.debug("Skipping view tracking for guest user on product: {}", productId);
            return;
        }

        try {
            // Use native query with ON DUPLICATE KEY UPDATE
            // This is atomic and efficient - single query
            viewRepository.trackView(
                uuidToBytes(userId),
                uuidToBytes(productId),
                LocalDateTime.now()
            );

            log.debug("Tracked view: user={}, product={}", userId, productId);

        } catch (Exception e) {
            // IMPORTANT: Just log error, don't throw exception
            // View tracking failure should NOT break main functionality
            log.error("Failed to track view: user={}, product={}, error={}",
                    userId, productId, e.getMessage());
        }
    }

    /**
     * Get user's recent views (for recommendation)
     *
     * @param userId User ID
     * @param days Number of days to look back
     * @return List of recent views
     */
    public List<UserProductView> getUserRecentViews(UUID userId, int days) {
        LocalDateTime sinceDate = LocalDateTime.now().minusDays(days);
        return viewRepository.findRecentViewsByUser(userId, sinceDate);
    }

    /**
     * Get user's most viewed products
     *
     * @param userId User ID
     * @return List of most viewed products
     */
    public List<UserProductView> getUserTopViews(UUID userId) {
        return viewRepository.findByUserIdOrderByViewCountDesc(userId);
    }

    /**
     * Get all user's views
     *
     * @param userId User ID
     * @return List of all views
     */
    public List<UserProductView> getUserViews(UUID userId) {
        return viewRepository.findByUserIdOrderByLastViewedAtDesc(userId);
    }

    /**
     * Get product analytics
     *
     * @param productId Product ID
     * @return Total view count
     */
    public Long getProductTotalViews(UUID productId) {
        return viewRepository.countTotalViewsByProduct(productId);
    }

    /**
     * Get unique viewer count
     *
     * @param productId Product ID
     * @return Number of unique viewers
     */
    public Long getProductUniqueViewers(UUID productId) {
        return viewRepository.countUniqueViewersByProduct(productId);
    }

    /**
     * Utility: Convert UUID to byte array for native queries
     */
    private byte[] uuidToBytes(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }
}
