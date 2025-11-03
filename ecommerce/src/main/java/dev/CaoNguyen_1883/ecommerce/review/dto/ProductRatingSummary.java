package dev.CaoNguyen_1883.ecommerce.review.dto;

import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRatingSummary {
    private UUID productId;
    private Double averageRating;
    private Long totalReviews;
    private Long fiveStarCount;
    private Long fourStarCount;
    private Long threeStarCount;
    private Long twoStarCount;
    private Long oneStarCount;
    
    // Percentages
    public Double getFiveStarPercentage() {
        return calculatePercentage(fiveStarCount);
    }
    
    public Double getFourStarPercentage() {
        return calculatePercentage(fourStarCount);
    }
    
    public Double getThreeStarPercentage() {
        return calculatePercentage(threeStarCount);
    }
    
    public Double getTwoStarPercentage() {
        return calculatePercentage(twoStarCount);
    }
    
    public Double getOneStarPercentage() {
        return calculatePercentage(oneStarCount);
    }
    
    private Double calculatePercentage(Long count) {
        if (totalReviews == null || totalReviews == 0) {
            return 0.0;
        }
        return (count * 100.0) / totalReviews;
    }
}