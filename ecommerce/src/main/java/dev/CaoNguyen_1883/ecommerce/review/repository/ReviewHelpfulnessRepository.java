package dev.CaoNguyen_1883.ecommerce.review.repository;

import dev.CaoNguyen_1883.ecommerce.review.entity.ReviewHelpfulness;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewHelpfulnessRepository extends JpaRepository<ReviewHelpfulness, UUID> {

    // Find user's vote for a review
    @Query("SELECT rh FROM ReviewHelpfulness rh " +
            "WHERE rh.review.id = :reviewId " +
            "AND rh.user.id = :userId")
    Optional<ReviewHelpfulness> findByReviewIdAndUserId(
            @Param("reviewId") UUID reviewId,
            @Param("userId") UUID userId
    );

    // Check if user has voted
    boolean existsByReviewIdAndUserId(UUID reviewId, UUID userId);

    // Delete user's vote
    void deleteByReviewIdAndUserId(UUID reviewId, UUID userId);
}