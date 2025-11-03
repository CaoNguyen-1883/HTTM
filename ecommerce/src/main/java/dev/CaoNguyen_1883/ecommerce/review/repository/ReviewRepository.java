package dev.CaoNguyen_1883.ecommerce.review.repository;

import dev.CaoNguyen_1883.ecommerce.review.entity.Review;
import dev.CaoNguyen_1883.ecommerce.review.entity.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    // Find review with all relationships
    @Query("SELECT DISTINCT r FROM Review r " +
            "LEFT JOIN FETCH r.images " +
            "LEFT JOIN FETCH r.user " +
            "LEFT JOIN FETCH r.product " +
            "WHERE r.id = :id")
    Optional<Review> findByIdWithDetails(@Param("id") UUID id);

    // Find reviews by product
    @Query("SELECT r FROM Review r " +
            "WHERE r.product.id = :productId " +
            "AND r.status = 'APPROVED' " +
            "AND r.isActive = true " +
            "ORDER BY r.createdAt DESC")
    Page<Review> findApprovedByProductId(@Param("productId") UUID productId, Pageable pageable);

    // Find reviews by product and rating
    @Query("SELECT r FROM Review r " +
            "WHERE r.product.id = :productId " +
            "AND r.rating = :rating " +
            "AND r.status = 'APPROVED' " +
            "AND r.isActive = true " +
            "ORDER BY r.createdAt DESC")
    Page<Review> findByProductIdAndRating(
            @Param("productId") UUID productId,
            @Param("rating") Integer rating,
            Pageable pageable
    );

    // Find reviews by user
    @Query("SELECT r FROM Review r " +
            "WHERE r.user.id = :userId " +
            "AND r.isActive = true " +
            "ORDER BY r.createdAt DESC")
    Page<Review> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    // Find reviews by status
    @Query("SELECT r FROM Review r " +
            "WHERE r.status = :status " +
            "AND r.isActive = true " +
            "ORDER BY r.createdAt ASC")
    Page<Review> findByStatus(@Param("status") ReviewStatus status, Pageable pageable);

    // Find pending reviews
    @Query("SELECT r FROM Review r " +
            "WHERE r.status = 'PENDING' " +
            "AND r.isActive = true " +
            "ORDER BY r.createdAt ASC")
    Page<Review> findPendingReviews(Pageable pageable);

    // Check if user already reviewed product
    @Query("SELECT COUNT(r) > 0 FROM Review r " +
            "WHERE r.product.id = :productId " +
            "AND r.user.id = :userId " +
            "AND r.isActive = true")
    boolean existsByProductIdAndUserId(
            @Param("productId") UUID productId,
            @Param("userId") UUID userId
    );

    // Check if user purchased product
    @Query("SELECT COUNT(o) > 0 FROM Order o " +
            "JOIN o.items oi " +
            "WHERE o.user.id = :userId " +
            "AND oi.variant.product.id = :productId " +
            "AND o.orderStatus = 'DELIVERED' " +
            "AND o.isActive = true")
    boolean hasUserPurchasedProduct(
            @Param("userId") UUID userId,
            @Param("productId") UUID productId
    );

    // Calculate average rating
    @Query("SELECT AVG(r.rating) FROM Review r " +
            "WHERE r.product.id = :productId " +
            "AND r.status = 'APPROVED' " +
            "AND r.isActive = true")
    Double calculateAverageRating(@Param("productId") UUID productId);

    // Count reviews by product
    @Query("SELECT COUNT(r) FROM Review r " +
            "WHERE r.product.id = :productId " +
            "AND r.status = 'APPROVED' " +
            "AND r.isActive = true")
    Long countApprovedByProductId(@Param("productId") UUID productId);

    // Count reviews by rating
    @Query("SELECT COUNT(r) FROM Review r " +
            "WHERE r.product.id = :productId " +
            "AND r.rating = :rating " +
            "AND r.status = 'APPROVED' " +
            "AND r.isActive = true")
    Long countByProductIdAndRating(
            @Param("productId") UUID productId,
            @Param("rating") Integer rating
    );

    // Get verified purchase reviews
    @Query("SELECT r FROM Review r " +
            "WHERE r.product.id = :productId " +
            "AND r.isVerifiedPurchase = true " +
            "AND r.status = 'APPROVED' " +
            "AND r.isActive = true " +
            "ORDER BY r.createdAt DESC")
    Page<Review> findVerifiedPurchasesByProductId(@Param("productId") UUID productId, Pageable pageable);

    // Get most helpful reviews
    @Query("SELECT r FROM Review r " +
            "WHERE r.product.id = :productId " +
            "AND r.status = 'APPROVED' " +
            "AND r.isActive = true " +
            "ORDER BY r.helpfulCount DESC, r.createdAt DESC")
    Page<Review> findMostHelpfulByProductId(@Param("productId") UUID productId, Pageable pageable);

    // Count reviews by status
    long countByStatus(ReviewStatus status);
}