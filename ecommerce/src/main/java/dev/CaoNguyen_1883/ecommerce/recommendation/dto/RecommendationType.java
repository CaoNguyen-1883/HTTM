package dev.CaoNguyen_1883.ecommerce.recommendation.dto;

public enum RecommendationType {
    TRENDING,           // Popular/trending products
    BEST_SELLERS,       // Top selling products
    TOP_RATED,          // Highest rated products
    NEW_ARRIVALS,       // Newest products
    SIMILAR_PRODUCTS,   // Similar to a specific product
    FREQUENTLY_BOUGHT_TOGETHER, // Bought together with a product
    FOR_YOU             // Personalized recommendations
}
