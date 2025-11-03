package dev.CaoNguyen_1883.ecommerce.review.entity;

import dev.CaoNguyen_1883.ecommerce.common.entity.BaseEntity;
import dev.CaoNguyen_1883.ecommerce.order.entity.Order;
import dev.CaoNguyen_1883.ecommerce.product.entity.Product;
import dev.CaoNguyen_1883.ecommerce.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reviews", indexes = {
        @Index(name = "idx_review_product", columnList = "product_id"),
        @Index(name = "idx_review_user", columnList = "user_id"),
        @Index(name = "idx_review_status", columnList = "status"),
        @Index(name = "idx_review_rating", columnList = "rating")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Review extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;  // Link to order for verified purchase

    @Column(nullable = false)
    private Integer rating;  // 1-5 stars

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReviewImage> images = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ReviewStatus status = ReviewStatus.PENDING;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isVerifiedPurchase = false;

    @Column(nullable = false)
    @Builder.Default
    private Integer helpfulCount = 0;  // Number of "helpful" votes

    @Column(nullable = false)
    @Builder.Default
    private Integer notHelpfulCount = 0;  // Number of "not helpful" votes

    // Moderation
    private LocalDateTime approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(columnDefinition = "TEXT")
    private String rejectionReason;

    // Seller/Admin reply
    @Column(columnDefinition = "TEXT")
    private String replyContent;

    private LocalDateTime repliedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replied_by")
    private User repliedBy;

    // Helper methods
    public void addImage(ReviewImage image) {
        images.add(image);
        image.setReview(this);
    }

    public void removeImage(ReviewImage image) {
        images.remove(image);
        image.setReview(null);
    }

    public void approve(User approvedBy) {
        if (this.status == ReviewStatus.APPROVED) {
            throw new IllegalStateException("Review is already approved");
        }
        this.status = ReviewStatus.APPROVED;
        this.approvedBy = approvedBy;
        this.approvedAt = LocalDateTime.now();
    }

    public void reject(String reason, User rejectedBy) {
        if (this.status == ReviewStatus.REJECTED) {
            throw new IllegalStateException("Review is already rejected");
        }
        this.status = ReviewStatus.REJECTED;
        this.rejectionReason = reason;
        this.approvedBy = rejectedBy;  // Use same field for rejected by
        this.approvedAt = LocalDateTime.now();
    }

    public void reply(String content, User repliedBy) {
        this.replyContent = content;
        this.repliedBy = repliedBy;
        this.repliedAt = LocalDateTime.now();
    }

    public void incrementHelpfulCount() {
        this.helpfulCount++;
    }

    public void decrementHelpfulCount() {
        if (this.helpfulCount > 0) {
            this.helpfulCount--;
        }
    }

    public void incrementNotHelpfulCount() {
        this.notHelpfulCount++;
    }

    public void decrementNotHelpfulCount() {
        if (this.notHelpfulCount > 0) {
            this.notHelpfulCount--;
        }
    }

    public Integer getTotalVotes() {
        return helpfulCount + notHelpfulCount;
    }

    public Double getHelpfulPercentage() {
        int total = getTotalVotes();
        if (total == 0) return 0.0;
        return (helpfulCount * 100.0) / total;
    }
}