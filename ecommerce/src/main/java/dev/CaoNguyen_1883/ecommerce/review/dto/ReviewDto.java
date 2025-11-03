package dev.CaoNguyen_1883.ecommerce.review.dto;

import dev.CaoNguyen_1883.ecommerce.review.entity.ReviewStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewDto {
    private UUID id;
    private UUID productId;
    private String productName;
    private UUID userId;
    private String userName;
    private String userAvatar;
    private UUID orderId;
    private Integer rating;
    private String title;
    private String content;
    private List<ReviewImageDto> images;
    private ReviewStatus status;
    private Boolean isVerifiedPurchase;
    private Integer helpfulCount;
    private Integer notHelpfulCount;
    private Integer totalVotes;
    private Double helpfulPercentage;
    
    // User's vote (if authenticated)
    private Boolean userVote;  // null = no vote, true = helpful, false = not helpful
    
    // Reply
    private String replyContent;
    private LocalDateTime repliedAt;
    private String repliedByName;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime approvedAt;
}