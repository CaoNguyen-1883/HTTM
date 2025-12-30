package dev.CaoNguyen_1883.ecommerce.recommendation.controller;

import dev.CaoNguyen_1883.ecommerce.auth.security.CustomUserDetails;
import dev.CaoNguyen_1883.ecommerce.common.response.ApiResponse;
import dev.CaoNguyen_1883.ecommerce.recommendation.dto.RecommendationDto;
import dev.CaoNguyen_1883.ecommerce.recommendation.service.IRecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@Tag(
    name = "Product Recommendations",
    description = "APIs for product recommendation system"
)
public class RecommendationController {

    private final IRecommendationService recommendationService;

    @Operation(
        summary = "Get homepage recommendations",
        description = "Get all recommendation sections for homepage (Public)"
    )
    @GetMapping("/homepage")
    public ResponseEntity<
        ApiResponse<List<RecommendationDto>>
    > getHomePageRecommendations() {
        List<RecommendationDto> recommendations =
            recommendationService.getHomePageRecommendations();
        return ResponseEntity.ok(
            ApiResponse.success(
                "Recommendations retrieved successfully",
                recommendations
            )
        );
    }

    @Operation(
        summary = "Get trending products",
        description = "Get currently trending products (Public)"
    )
    @GetMapping("/trending")
    public ResponseEntity<ApiResponse<RecommendationDto>> getTrendingProducts(
        @Parameter(description = "Number of products to return") @RequestParam(
            defaultValue = "8"
        ) int limit
    ) {
        RecommendationDto recommendation =
            recommendationService.getTrendingProducts(limit);
        return ResponseEntity.ok(
            ApiResponse.success(
                "Trending products retrieved successfully",
                recommendation
            )
        );
    }

    @Operation(
        summary = "Get best sellers",
        description = "Get best selling products (Public)"
    )
    @GetMapping("/best-sellers")
    public ResponseEntity<
        ApiResponse<RecommendationDto>
    > getBestSellingProducts(
        @Parameter(description = "Number of products to return") @RequestParam(
            defaultValue = "8"
        ) int limit
    ) {
        RecommendationDto recommendation =
            recommendationService.getBestSellingProducts(limit);
        return ResponseEntity.ok(
            ApiResponse.success(
                "Best sellers retrieved successfully",
                recommendation
            )
        );
    }

    @Operation(
        summary = "Get top rated products",
        description = "Get highest rated products (Public)"
    )
    @GetMapping("/top-rated")
    public ResponseEntity<ApiResponse<RecommendationDto>> getTopRatedProducts(
        @Parameter(description = "Number of products to return") @RequestParam(
            defaultValue = "8"
        ) int limit
    ) {
        RecommendationDto recommendation =
            recommendationService.getTopRatedProducts(limit);
        return ResponseEntity.ok(
            ApiResponse.success(
                "Top rated products retrieved successfully",
                recommendation
            )
        );
    }

    @Operation(
        summary = "Get new arrivals",
        description = "Get newest products (Public)"
    )
    @GetMapping("/new-arrivals")
    public ResponseEntity<ApiResponse<RecommendationDto>> getNewArrivals(
        @Parameter(description = "Number of products to return") @RequestParam(
            defaultValue = "8"
        ) int limit
    ) {
        RecommendationDto recommendation = recommendationService.getNewArrivals(
            limit
        );
        return ResponseEntity.ok(
            ApiResponse.success(
                "New arrivals retrieved successfully",
                recommendation
            )
        );
    }

    @Operation(
        summary = "Get similar products",
        description = "Get products similar to a specific product (Public)"
    )
    @GetMapping("/similar/{productId}")
    public ResponseEntity<ApiResponse<RecommendationDto>> getSimilarProducts(
        @PathVariable UUID productId,
        @Parameter(description = "Number of products to return") @RequestParam(
            defaultValue = "8"
        ) int limit
    ) {
        RecommendationDto recommendation =
            recommendationService.getSimilarProducts(productId, limit);
        return ResponseEntity.ok(
            ApiResponse.success(
                "Similar products retrieved successfully",
                recommendation
            )
        );
    }

    @Operation(
        summary = "Get frequently bought together",
        description = "Get products frequently bought together with a specific product (Public)"
    )
    @GetMapping("/bought-together/{productId}")
    public ResponseEntity<
        ApiResponse<RecommendationDto>
    > getFrequentlyBoughtTogether(
        @PathVariable UUID productId,
        @Parameter(description = "Number of products to return") @RequestParam(
            defaultValue = "5"
        ) int limit
    ) {
        RecommendationDto recommendation =
            recommendationService.getFrequentlyBoughtTogether(productId, limit);
        return ResponseEntity.ok(
            ApiResponse.success(
                "Recommendations retrieved successfully",
                recommendation
            )
        );
    }

    @Operation(
        summary = "Get personalized recommendations",
        description = "AI-powered recommendations using ML (Hybrid: CF 70% + CB 30%). For authenticated users based on cart/purchase history, for guests based on trending products."
    )
    @GetMapping("/for-you")
    public ResponseEntity<ApiResponse<RecommendationDto>> getRecommendationsForYou(
        Authentication authentication,
        @Parameter(description = "Number of products to return")
        @RequestParam(defaultValue = "20") int limit
    ) {
        if (authentication != null && authentication.isAuthenticated()) {
            CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();
            RecommendationDto recommendation =
                recommendationService.getRecommendationsBasedOnCart(
                    userDetails.getId(),
                    limit
                );
            return ResponseEntity.ok(
                ApiResponse.success(
                    "AI-powered recommendations retrieved successfully",
                    recommendation
                )
            );
        } else {
            // Return trending for unauthenticated users
            RecommendationDto recommendation =
                recommendationService.getTrendingProducts(limit);
            return ResponseEntity.ok(
                ApiResponse.success(
                    "Trending recommendations retrieved successfully",
                    recommendation
                )
            );
        }
    }
}
