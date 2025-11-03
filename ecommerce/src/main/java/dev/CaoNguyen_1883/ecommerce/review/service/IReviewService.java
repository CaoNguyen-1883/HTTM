package dev.CaoNguyen_1883.ecommerce.review.service;

import dev.CaoNguyen_1883.ecommerce.review.dto.*;
import dev.CaoNguyen_1883.ecommerce.review.entity.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface IReviewService {

    /**
     * Create a new review
     */
    @Transactional
    ReviewDto createReview(UUID userId, CreateReviewRequest request);

    /**
     * Update review
     */
    @Transactional
    ReviewDto updateReview(UUID reviewId, UUID userId, UpdateReviewRequest request);

    /**
     * Delete review (soft delete)
     */
    @Transactional
    void deleteReview(UUID reviewId, UUID userId);

    /**
     * Get review by ID
     */
    ReviewDto getReviewById(UUID reviewId, UUID currentUserId);

    /**
     * Get approved reviews by product
     */
    Page<ReviewDto> getProductReviews(UUID productId, UUID currentUserId, Pageable pageable);

    /**
     * Get reviews by product and rating
     */
    Page<ReviewDto> getProductReviewsByRating(UUID productId, Integer rating, UUID currentUserId, Pageable pageable);

    /**
     * Get user's reviews
     */
    Page<ReviewDto> getUserReviews(UUID userId, Pageable pageable);

    /**
     * Get reviews by status (admin/staff)
     */
    Page<ReviewDto> getReviewsByStatus(ReviewStatus status, Pageable pageable);

    /**
     * Get pending reviews (admin/staff)
     */
    Page<ReviewDto> getPendingReviews(Pageable pageable);

    /**
     * Get verified purchase reviews
     */
    Page<ReviewDto> getVerifiedPurchaseReviews(UUID productId, UUID currentUserId, Pageable pageable);

    /**
     * Get most helpful reviews
     */
    Page<ReviewDto> getMostHelpfulReviews(UUID productId, UUID currentUserId, Pageable pageable);

    /**
     * Approve review (admin/staff)
     */
    @Transactional
    ReviewDto approveReview(UUID reviewId, UUID approvedBy);

    /**
     * Reject review (admin/staff)
     */
    @Transactional
    ReviewDto rejectReview(UUID reviewId, RejectReviewRequest request, UUID rejectedBy);

    /**
     * Reply to review (seller/admin)
     */
    @Transactional
    ReviewDto replyToReview(UUID reviewId, ReplyReviewRequest request, UUID repliedBy);

    /**
     * Vote review as helpful/not helpful
     */
    @Transactional
    ReviewDto voteReview(UUID reviewId, UUID userId, boolean isHelpful);

    /**
     * Remove vote
     */
    @Transactional
    ReviewDto removeVote(UUID reviewId, UUID userId);

    /**
     * Get product rating summary
     */
    ProductRatingSummary getProductRatingSummary(UUID productId);

    /**
     * Update product rating statistics
     */
    @Transactional
    void updateProductRatingStatistics(UUID productId);
}