package dev.CaoNguyen_1883.ecommerce.review.service.impl;

import dev.CaoNguyen_1883.ecommerce.common.exception.BadRequestException;
import dev.CaoNguyen_1883.ecommerce.common.exception.ForbiddenException;
import dev.CaoNguyen_1883.ecommerce.common.exception.ResourceNotFoundException;
import dev.CaoNguyen_1883.ecommerce.order.entity.Order;
import dev.CaoNguyen_1883.ecommerce.order.repository.OrderRepository;
import dev.CaoNguyen_1883.ecommerce.product.entity.Product;
import dev.CaoNguyen_1883.ecommerce.product.repository.ProductRepository;
import dev.CaoNguyen_1883.ecommerce.review.dto.*;
import dev.CaoNguyen_1883.ecommerce.review.entity.Review;
import dev.CaoNguyen_1883.ecommerce.review.entity.ReviewHelpfulness;
import dev.CaoNguyen_1883.ecommerce.review.entity.ReviewImage;
import dev.CaoNguyen_1883.ecommerce.review.entity.ReviewStatus;
import dev.CaoNguyen_1883.ecommerce.review.mapper.ReviewMapper;
import dev.CaoNguyen_1883.ecommerce.review.repository.ReviewHelpfulnessRepository;
import dev.CaoNguyen_1883.ecommerce.review.repository.ReviewImageRepository;
import dev.CaoNguyen_1883.ecommerce.review.repository.ReviewRepository;
import dev.CaoNguyen_1883.ecommerce.review.service.IReviewService;
import dev.CaoNguyen_1883.ecommerce.user.entity.User;
import dev.CaoNguyen_1883.ecommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReviewServiceImpl implements IReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final ReviewHelpfulnessRepository helpfulnessRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ReviewMapper reviewMapper;

    @Override
    @Transactional
    public ReviewDto createReview(UUID userId, CreateReviewRequest request) {
        log.debug("Creating review for product: {} by user: {}", request.getProductId(), userId);

        // Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Get product
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));

        // Check if user already reviewed this product
        if (reviewRepository.existsByProductIdAndUserId(request.getProductId(), userId)) {
            throw new BadRequestException("You have already reviewed this product");
        }

        // Check if verified purchase
        boolean isVerifiedPurchase = false;
        Order order = null;

        if (request.getOrderId() != null) {
            order = orderRepository.findById(request.getOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Order", "id", request.getOrderId()));

            // Verify order belongs to user and contains this product
            if (!order.getUser().getId().equals(userId)) {
                throw new ForbiddenException("Order does not belong to you");
            }

            boolean containsProduct = order.getItems().stream()
                    .anyMatch(item -> item.getVariant().getProduct().getId().equals(request.getProductId()));

            if (!containsProduct) {
                throw new BadRequestException("Order does not contain this product");
            }

            if (order.getOrderStatus() == dev.CaoNguyen_1883.ecommerce.order.entity.OrderStatus.DELIVERED) {
                isVerifiedPurchase = true;
            }
        } else {
            // Check if user has purchased this product in any order
            isVerifiedPurchase = reviewRepository.hasUserPurchasedProduct(userId, request.getProductId());
        }

        // Create review
        Review review = Review.builder()
                .product(product)
                .user(user)
                .order(order)
                .rating(request.getRating())
                .title(request.getTitle())
                .content(request.getContent())
                .isVerifiedPurchase(isVerifiedPurchase)
                .build();

        // Add images
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            for (int i = 0; i < request.getImageUrls().size(); i++) {
                ReviewImage image = ReviewImage.builder()
                        .review(review)
                        .imageUrl(request.getImageUrls().get(i))
                        .displayOrder(i)
                        .build();
                review.addImage(image);
            }
        }

        review = reviewRepository.save(review);

        log.info("Review created. Product: {}, User: {}, Rating: {}, Verified: {}",
                request.getProductId(), userId, request.getRating(), isVerifiedPurchase);

        return reviewMapper.toDto(review);
    }

    @Override
    @Transactional
    public ReviewDto updateReview(UUID reviewId, UUID userId, UpdateReviewRequest request) {
        log.debug("Updating review: {} by user: {}", reviewId, userId);

        Review review = reviewRepository.findByIdWithDetails(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        // Verify ownership
        if (!review.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You can only update your own reviews");
        }

        // Only allow update if pending or approved
        if (review.getStatus() == ReviewStatus.REJECTED) {
            throw new BadRequestException("Cannot update rejected review");
        }

        // Update fields
        review.setRating(request.getRating());
        review.setTitle(request.getTitle());
        review.setContent(request.getContent());

        // Reset to pending if it was approved
        if (review.getStatus() == ReviewStatus.APPROVED) {
            review.setStatus(ReviewStatus.PENDING);
            review.setApprovedBy(null);
            review.setApprovedAt(null);
        }

        // Update images
        review.getImages().clear();
        reviewImageRepository.softDeleteByReviewId(reviewId);

        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            for (int i = 0; i < request.getImageUrls().size(); i++) {
                ReviewImage image = ReviewImage.builder()
                        .review(review)
                        .imageUrl(request.getImageUrls().get(i))
                        .displayOrder(i)
                        .build();
                review.addImage(image);
            }
        }

        review = reviewRepository.save(review);

        log.info("Review updated. ID: {}, User: {}", reviewId, userId);

        return reviewMapper.toDto(review);
    }

    @Override
    @Transactional
    public void deleteReview(UUID reviewId, UUID userId) {
        log.debug("Deleting review: {} by user: {}", reviewId, userId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        // Verify ownership
        if (!review.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You can only delete your own reviews");
        }

        review.softDelete();
        reviewRepository.save(review);

        // Update product rating statistics
        updateProductRatingStatistics(review.getProduct().getId());

        log.info("Review deleted. ID: {}, User: {}", reviewId, userId);
    }

    @Override
    public ReviewDto getReviewById(UUID reviewId, UUID currentUserId) {
        log.debug("Fetching review: {}", reviewId);

        Review review = reviewRepository.findByIdWithDetails(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        ReviewDto dto = reviewMapper.toDto(review);

        // Set user's vote if authenticated
        if (currentUserId != null) {
            setUserVote(dto, reviewId, currentUserId);
        }

        return dto;
    }

    @Override
    public Page<ReviewDto> getProductReviews(UUID productId, UUID currentUserId, Pageable pageable) {
        log.debug("Fetching reviews for product: {}", productId);

        Page<Review> reviews = reviewRepository.findApprovedByProductId(productId, pageable);
        Page<ReviewDto> dtos = reviews.map(reviewMapper::toDto);

        // Set user's votes if authenticated
        if (currentUserId != null) {
            dtos.forEach(dto -> setUserVote(dto, dto.getId(), currentUserId));
        }

        return dtos;
    }

    @Override
    public Page<ReviewDto> getProductReviewsByRating(UUID productId, Integer rating, UUID currentUserId, Pageable pageable) {
        log.debug("Fetching reviews for product: {} with rating: {}", productId, rating);

        Page<Review> reviews = reviewRepository.findByProductIdAndRating(productId, rating, pageable);
        Page<ReviewDto> dtos = reviews.map(reviewMapper::toDto);

        // Set user's votes if authenticated
        if (currentUserId != null) {
            dtos.forEach(dto -> setUserVote(dto, dto.getId(), currentUserId));
        }

        return dtos;
    }

    @Override
    public Page<ReviewDto> getUserReviews(UUID userId, Pageable pageable) {
        log.debug("Fetching reviews for user: {}", userId);

        return reviewRepository.findByUserId(userId, pageable)
                .map(reviewMapper::toDto);
    }

    @Override
    public Page<ReviewDto> getReviewsByStatus(ReviewStatus status, Pageable pageable) {
        log.debug("Fetching reviews by status: {}", status);

        return reviewRepository.findByStatus(status, pageable)
                .map(reviewMapper::toDto);
    }

    @Override
    public Page<ReviewDto> getPendingReviews(Pageable pageable) {
        log.debug("Fetching pending reviews");

        return reviewRepository.findPendingReviews(pageable)
                .map(reviewMapper::toDto);
    }

    @Override
    public Page<ReviewDto> getVerifiedPurchaseReviews(UUID productId, UUID currentUserId, Pageable pageable) {
        log.debug("Fetching verified purchase reviews for product: {}", productId);

        Page<Review> reviews = reviewRepository.findVerifiedPurchasesByProductId(productId, pageable);
        Page<ReviewDto> dtos = reviews.map(reviewMapper::toDto);

        // Set user's votes if authenticated
        if (currentUserId != null) {
            dtos.forEach(dto -> setUserVote(dto, dto.getId(), currentUserId));
        }

        return dtos;
    }

    @Override
    public Page<ReviewDto> getMostHelpfulReviews(UUID productId, UUID currentUserId, Pageable pageable) {
        log.debug("Fetching most helpful reviews for product: {}", productId);

        Page<Review> reviews = reviewRepository.findMostHelpfulByProductId(productId, pageable);
        Page<ReviewDto> dtos = reviews.map(reviewMapper::toDto);

        // Set user's votes if authenticated
        if (currentUserId != null) {
            dtos.forEach(dto -> setUserVote(dto, dto.getId(), currentUserId));
        }

        return dtos;
    }

    @Override
    @Transactional
    public ReviewDto approveReview(UUID reviewId, UUID approvedBy) {
        log.debug("Approving review: {}", reviewId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        User approver = userRepository.findById(approvedBy)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", approvedBy));

        review.approve(approver);
        review = reviewRepository.save(review);

        // Update product rating statistics
        updateProductRatingStatistics(review.getProduct().getId());

        log.info("Review approved. ID: {}, Approved by: {}", reviewId, approver.getEmail());

        return reviewMapper.toDto(review);
    }

    @Override
    @Transactional
    public ReviewDto rejectReview(UUID reviewId, RejectReviewRequest request, UUID rejectedBy) {
        log.debug("Rejecting review: {}", reviewId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        User rejector = userRepository.findById(rejectedBy)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", rejectedBy));

        review.reject(request.getReason(), rejector);
        review = reviewRepository.save(review);

        log.info("Review rejected. ID: {}, Rejected by: {}, Reason: {}",
                reviewId, rejector.getEmail(), request.getReason());

        return reviewMapper.toDto(review);
    }

    @Override
    @Transactional
    public ReviewDto replyToReview(UUID reviewId, ReplyReviewRequest request, UUID repliedBy) {
        log.debug("Replying to review: {}", reviewId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        User replier = userRepository.findById(repliedBy)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", repliedBy));

        review.reply(request.getContent(), replier);
        review = reviewRepository.save(review);

        log.info("Review reply added. ID: {}, Replied by: {}", reviewId, replier.getEmail());

        return reviewMapper.toDto(review);
    }

    @Override
    @Transactional
    public ReviewDto voteReview(UUID reviewId, UUID userId, boolean isHelpful) {
        log.debug("Voting review: {} as {} by user: {}", reviewId, isHelpful ? "helpful" : "not helpful", userId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Check if user already voted
        ReviewHelpfulness existingVote = helpfulnessRepository.findByReviewIdAndUserId(reviewId, userId)
                .orElse(null);

        if (existingVote != null) {
            // Update existing vote
            boolean oldVote = existingVote.getIsHelpful();

            if (oldVote == isHelpful) {
                throw new BadRequestException("You have already voted this way");
            }

            // Update counts
            if (oldVote) {
                review.decrementHelpfulCount();
                review.incrementNotHelpfulCount();
            } else {
                review.decrementNotHelpfulCount();
                review.incrementHelpfulCount();
            }

            existingVote.setIsHelpful(isHelpful);
            helpfulnessRepository.save(existingVote);
        } else {
            // Create new vote
            ReviewHelpfulness vote = ReviewHelpfulness.builder()
                    .review(review)
                    .user(user)
                    .isHelpful(isHelpful)
                    .build();

            helpfulnessRepository.save(vote);

            // Update counts
            if (isHelpful) {
                review.incrementHelpfulCount();
            } else {
                review.incrementNotHelpfulCount();
            }
        }

        review = reviewRepository.save(review);

        log.info("Review voted. ID: {}, User: {}, Helpful: {}", reviewId, userId, isHelpful);

        ReviewDto dto = reviewMapper.toDto(review);
        dto.setUserVote(isHelpful);
        return dto;
    }

    @Override
    @Transactional
    public ReviewDto removeVote(UUID reviewId, UUID userId) {
        log.debug("Removing vote from review: {} by user: {}", reviewId, userId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        ReviewHelpfulness vote = helpfulnessRepository.findByReviewIdAndUserId(reviewId, userId)
                .orElseThrow(() -> new BadRequestException("You have not voted on this review"));

        // Update counts
        if (vote.getIsHelpful()) {
            review.decrementHelpfulCount();
        } else {
            review.decrementNotHelpfulCount();
        }

        helpfulnessRepository.delete(vote);
        review = reviewRepository.save(review);

        log.info("Vote removed. Review: {}, User: {}", reviewId, userId);

        ReviewDto dto = reviewMapper.toDto(review);
        dto.setUserVote(null);
        return dto;
    }

    @Override
    public ProductRatingSummary getProductRatingSummary(UUID productId) {
        log.debug("Fetching rating summary for product: {}", productId);

        Double averageRating = reviewRepository.calculateAverageRating(productId);
        Long totalReviews = reviewRepository.countApprovedByProductId(productId);

        Long fiveStars = reviewRepository.countByProductIdAndRating(productId, 5);
        Long fourStars = reviewRepository.countByProductIdAndRating(productId, 4);
        Long threeStars = reviewRepository.countByProductIdAndRating(productId, 3);
        Long twoStars = reviewRepository.countByProductIdAndRating(productId, 2);
        Long oneStars = reviewRepository.countByProductIdAndRating(productId, 1);

        return ProductRatingSummary.builder()
                .productId(productId)
                .averageRating(averageRating != null ? averageRating : 0.0)
                .totalReviews(totalReviews)
                .fiveStarCount(fiveStars)
                .fourStarCount(fourStars)
                .threeStarCount(threeStars)
                .twoStarCount(twoStars)
                .oneStarCount(oneStars)
                .build();
    }

    @Override
    @Transactional
    public void updateProductRatingStatistics(UUID productId) {
        log.debug("Updating product rating statistics for: {}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        Double averageRating = reviewRepository.calculateAverageRating(productId);
        Long totalReviews = reviewRepository.countApprovedByProductId(productId);

        product.setAverageRating(averageRating != null ? averageRating : 0.0);
        product.setTotalReviews(totalReviews != null ? totalReviews.intValue() : 0);

        productRepository.save(product);

        log.info("Product rating statistics updated. Product: {}, Avg: {}, Total: {}",
                productId, averageRating, totalReviews);
    }

    // ===== HELPER METHODS =====

    private void setUserVote(ReviewDto dto, UUID reviewId, UUID userId) {
        helpfulnessRepository.findByReviewIdAndUserId(reviewId, userId)
                .ifPresent(vote -> dto.setUserVote(vote.getIsHelpful()));
    }
}