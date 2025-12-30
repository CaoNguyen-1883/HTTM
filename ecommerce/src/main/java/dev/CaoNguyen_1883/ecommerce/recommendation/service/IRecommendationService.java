package dev.CaoNguyen_1883.ecommerce.recommendation.service;

import dev.CaoNguyen_1883.ecommerce.recommendation.dto.RecommendationDto;
import java.util.List;
import java.util.UUID;

public interface IRecommendationService {
    /**
     * Get all recommendations for homepage
     */
    List<RecommendationDto> getHomePageRecommendations();

    /**
     * Get trending products
     */
    RecommendationDto getTrendingProducts(int limit);

    /**
     * Get best selling products
     */
    RecommendationDto getBestSellingProducts(int limit);

    /**
     * Get top rated products
     */
    RecommendationDto getTopRatedProducts(int limit);

    /**
     * Get new arrivals (newest products)
     */
    RecommendationDto getNewArrivals(int limit);

    /**
     * Get products similar to a specific product
     */
    RecommendationDto getSimilarProducts(UUID productId, int limit);

    /**
     * Get products frequently bought together with a specific product
     */
    RecommendationDto getFrequentlyBoughtTogether(UUID productId, int limit);

    /**
     * Get personalized recommendations based on user's cart
     */
    RecommendationDto getRecommendationsBasedOnCart(UUID userId, int limit);

    /**
     * Get personalized recommendations based on user's view history
     */
    RecommendationDto getRecommendationsBasedOnViewHistory(
        UUID userId,
        int limit
    );
}
