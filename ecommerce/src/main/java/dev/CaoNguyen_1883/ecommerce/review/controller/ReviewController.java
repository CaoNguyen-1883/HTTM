package dev.CaoNguyen_1883.ecommerce.review.controller;

import dev.CaoNguyen_1883.ecommerce.auth.security.CustomUserDetails;
import dev.CaoNguyen_1883.ecommerce.common.response.ApiResponse;
import dev.CaoNguyen_1883.ecommerce.review.dto.*;
import dev.CaoNguyen_1883.ecommerce.review.entity.ReviewStatus;
import dev.CaoNguyen_1883.ecommerce.review.service.IReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Review Management", description = "APIs for managing product reviews and ratings")
public class ReviewController {

    private final IReviewService reviewService;

    // ===== PUBLIC ENDPOINTS =====

    @Operation(
            summary = "Get product reviews",
            description = "Get all approved reviews for a product (Public)"
    )
    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<Page<ReviewDto>>> getProductReviews(
            @PathVariable UUID productId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {
        UUID currentUserId = getCurrentUserId(authentication);
        Page<ReviewDto> reviews = reviewService.getProductReviews(productId, currentUserId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Reviews retrieved successfully", reviews));
    }

    @Operation(
            summary = "Get product reviews by rating",
            description = "Filter product reviews by rating (Public)"
    )
    @GetMapping("/product/{productId}/rating/{rating}")
    public ResponseEntity<ApiResponse<Page<ReviewDto>>> getProductReviewsByRating(
            @PathVariable UUID productId,
            @PathVariable Integer rating,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {
        UUID currentUserId = getCurrentUserId(authentication);
        Page<ReviewDto> reviews = reviewService.getProductReviewsByRating(productId, rating, currentUserId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Reviews retrieved successfully", reviews));
    }

    @Operation(
            summary = "Get verified purchase reviews",
            description = "Get reviews from verified purchases only (Public)"
    )
    @GetMapping("/product/{productId}/verified")
    public ResponseEntity<ApiResponse<Page<ReviewDto>>> getVerifiedPurchaseReviews(
            @PathVariable UUID productId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {
        UUID currentUserId = getCurrentUserId(authentication);
        Page<ReviewDto> reviews = reviewService.getVerifiedPurchaseReviews(productId, currentUserId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Verified reviews retrieved successfully", reviews));
    }

    @Operation(
            summary = "Get most helpful reviews",
            description = "Get reviews sorted by helpfulness (Public)"
    )
    @GetMapping("/product/{productId}/helpful")
    public ResponseEntity<ApiResponse<Page<ReviewDto>>> getMostHelpfulReviews(
            @PathVariable UUID productId,
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication) {
        UUID currentUserId = getCurrentUserId(authentication);
        Page<ReviewDto> reviews = reviewService.getMostHelpfulReviews(productId, currentUserId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Most helpful reviews retrieved successfully", reviews));
    }

    @Operation(
            summary = "Get product rating summary",
            description = "Get rating statistics for a product (Public)"
    )
    @GetMapping("/product/{productId}/summary")
    public ResponseEntity<ApiResponse<ProductRatingSummary>> getProductRatingSummary(
            @PathVariable UUID productId) {
        ProductRatingSummary summary = reviewService.getProductRatingSummary(productId);
        return ResponseEntity.ok(ApiResponse.success("Rating summary retrieved successfully", summary));
    }

    @Operation(
            summary = "Get review by ID",
            description = "Get review details by ID (Public)"
    )
    @GetMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewDto>> getReviewById(
            @PathVariable UUID reviewId,
            Authentication authentication) {
        UUID currentUserId = getCurrentUserId(authentication);
        ReviewDto review = reviewService.getReviewById(reviewId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Review retrieved successfully", review));
    }

    // ===== CUSTOMER ENDPOINTS =====

    @Operation(
            summary = "Create review",
            description = "Create a new product review"
    )
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseEntity<ApiResponse<ReviewDto>> createReview(
            @Valid @RequestBody CreateReviewRequest request,
            Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UUID userId = userDetails.getId();

        ReviewDto review = reviewService.createReview(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Review created successfully", review));
    }

    @Operation(
            summary = "Update review",
            description = "Update your own review"
    )
    @PutMapping("/{reviewId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseEntity<ApiResponse<ReviewDto>> updateReview(
            @PathVariable UUID reviewId,
            @Valid @RequestBody UpdateReviewRequest request,
            Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UUID userId = userDetails.getId();

        ReviewDto review = reviewService.updateReview(reviewId, userId, request);
        return ResponseEntity.ok(ApiResponse.success("Review updated successfully", review));
    }

    @Operation(
            summary = "Delete review",
            description = "Delete your own review"
    )
    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @PathVariable UUID reviewId,
            Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UUID userId = userDetails.getId();

        reviewService.deleteReview(reviewId, userId);
        return ResponseEntity.ok(ApiResponse.success("Review deleted successfully", null));
    }

    @Operation(
            summary = "Get my reviews",
            description = "Get all reviews written by current user"
    )
    @GetMapping("/my-reviews")
    @PreAuthorize("hasRole('CUSTOMER')")
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseEntity<ApiResponse<Page<ReviewDto>>> getMyReviews(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UUID userId = userDetails.getId();

        Page<ReviewDto> reviews = reviewService.getUserReviews(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Your reviews retrieved successfully", reviews));
    }

    @Operation(
            summary = "Vote review as helpful",
            description = "Vote a review as helpful or not helpful"
    )
    @PostMapping("/{reviewId}/vote")
    @PreAuthorize("hasRole('CUSTOMER')")
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseEntity<ApiResponse<ReviewDto>> voteReview(
            @PathVariable UUID reviewId,
            @Parameter(description = "true = helpful, false = not helpful")
            @RequestParam boolean isHelpful,
            Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UUID userId = userDetails.getId();

        ReviewDto review = reviewService.voteReview(reviewId, userId, isHelpful);
        return ResponseEntity.ok(ApiResponse.success("Vote recorded successfully", review));
    }

    @Operation(
            summary = "Remove vote",
            description = "Remove your vote from a review"
    )
    @DeleteMapping("/{reviewId}/vote")
    @PreAuthorize("hasRole('CUSTOMER')")
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseEntity<ApiResponse<ReviewDto>> removeVote(
            @PathVariable UUID reviewId,
            Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UUID userId = userDetails.getId();

        ReviewDto review = reviewService.removeVote(reviewId, userId);
        return ResponseEntity.ok(ApiResponse.success("Vote removed successfully", review));
    }

    // ===== ADMIN/STAFF ENDPOINTS =====

    @Operation(
            summary = "Get reviews by status",
            description = "Get reviews filtered by status (Admin/Staff only)"
    )
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseEntity<ApiResponse<Page<ReviewDto>>> getReviewsByStatus(
            @PathVariable ReviewStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<ReviewDto> reviews = reviewService.getReviewsByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success("Reviews retrieved successfully", reviews));
    }

    @Operation(
            summary = "Get pending reviews",
            description = "Get all pending reviews waiting for moderation (Admin/Staff only)"
    )
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseEntity<ApiResponse<Page<ReviewDto>>> getPendingReviews(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<ReviewDto> reviews = reviewService.getPendingReviews(pageable);
        return ResponseEntity.ok(ApiResponse.success("Pending reviews retrieved successfully", reviews));
    }

    @Operation(
            summary = "Approve review",
            description = "Approve a pending review (Admin/Staff only)"
    )
    @PostMapping("/{reviewId}/approve")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseEntity<ApiResponse<ReviewDto>> approveReview(
            @PathVariable UUID reviewId,
            Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UUID approvedBy = userDetails.getId();

        ReviewDto review = reviewService.approveReview(reviewId, approvedBy);
        return ResponseEntity.ok(ApiResponse.success("Review approved successfully", review));
    }

    @Operation(
            summary = "Reject review",
            description = "Reject a review (Admin/Staff only)"
    )
    @PostMapping("/{reviewId}/reject")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseEntity<ApiResponse<ReviewDto>> rejectReview(
            @PathVariable UUID reviewId,
            @Valid @RequestBody RejectReviewRequest request,
            Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UUID rejectedBy = userDetails.getId();

        ReviewDto review = reviewService.rejectReview(reviewId, request, rejectedBy);
        return ResponseEntity.ok(ApiResponse.success("Review rejected successfully", review));
    }

    @Operation(
            summary = "Reply to review",
            description = "Reply to a review (Seller/Admin only)"
    )
    @PostMapping("/{reviewId}/reply")
    @PreAuthorize("hasAnyRole('SELLER', 'STAFF', 'ADMIN')")
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseEntity<ApiResponse<ReviewDto>> replyToReview(
            @PathVariable UUID reviewId,
            @Valid @RequestBody ReplyReviewRequest request,
            Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UUID repliedBy = userDetails.getId();

        ReviewDto review = reviewService.replyToReview(reviewId, request, repliedBy);
        return ResponseEntity.ok(ApiResponse.success("Reply added successfully", review));
    }

    // ===== HELPER METHODS =====

    private UUID getCurrentUserId(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof CustomUserDetails) {
                return ((CustomUserDetails) principal).getId();
            }
        }
        return null;
    }
}