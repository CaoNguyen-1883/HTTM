package dev.CaoNguyen_1883.ecommerce.recommendation.client;

import dev.CaoNguyen_1883.ecommerce.recommendation.dto.ml.MlRecommendationResponse;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
@Slf4j
public class MlRecommendationClient {

    private final RestTemplate restTemplate;

    @Value("${ml.recommendation.api.enabled:true}")
    private boolean mlApiEnabled;

    /**
     * Get hybrid recommendations for user based on viewed products
     * @param userId User UUID
     * @param viewedProductIds List of viewed product UUIDs
     * @param limit Max results
     * @return List of recommendations with scores
     */
    public Optional<MlRecommendationResponse> getForYouRecommendations(
        UUID userId,
        List<UUID> viewedProductIds,
        int limit
    ) {
        if (!mlApiEnabled) {
            log.debug("ML API disabled, returning empty");
            return Optional.empty();
        }

        try {
            // Convert product IDs to comma-separated string
            String viewedProductsStr = viewedProductIds
                .stream()
                .map(UUID::toString)
                .collect(java.util.stream.Collectors.joining(","));

            // Build query params
            String url = UriComponentsBuilder.fromPath(
                "/api/v2/recommendations/for-you"
            )
                .queryParam("user_id", userId.toString())
                .queryParam("viewed_products", viewedProductsStr)
                .queryParam("limit", limit)
                .toUriString();

            log.debug("Calling ML API: {}", url);

            MlRecommendationResponse response = restTemplate.getForObject(
                url,
                MlRecommendationResponse.class
            );

            log.info(
                "ML API returned {} recommendations for user {}",
                response != null ? response.getTotal() : 0,
                userId
            );

            log.info("Response from ML API: {}", response);

            return Optional.ofNullable(response);
        } catch (Exception e) {
            log.error(
                "Error calling ML API for user {}: {}",
                userId,
                e.getMessage()
            );
            return Optional.empty();
        }
    }

    /**
     * Get similar products using content-based filtering
     * @param productId Product UUID
     * @param limit Max results
     * @return List of similar products with scores
     */
    public Optional<MlRecommendationResponse> getSimilarProducts(
        UUID productId,
        int limit
    ) {
        if (!mlApiEnabled) {
            return Optional.empty();
        }

        try {
            String url = String.format(
                "/api/v2/recommendations/similar/%s?limit=%d",
                productId,
                limit
            );

            log.debug("Calling ML API: {}", url);

            MlRecommendationResponse response = restTemplate.getForObject(
                url,
                MlRecommendationResponse.class
            );

            log.info(
                "ML API returned {} similar products for {}",
                response != null ? response.getTotal() : 0,
                productId
            );

            return Optional.ofNullable(response);
        } catch (Exception e) {
            log.error(
                "Error calling ML API for product {}: {}",
                productId,
                e.getMessage()
            );
            return Optional.empty();
        }
    }

    /**
     * Get cross-sell recommendations using collaborative filtering
     * @param productId Product UUID
     * @param limit Max results
     * @return List of cross-sell products with scores
     */
    public Optional<MlRecommendationResponse> getCrossSellProducts(
        UUID productId,
        int limit
    ) {
        if (!mlApiEnabled) {
            return Optional.empty();
        }

        try {
            String url = String.format(
                "/api/v2/recommendations/cross-sell/%s?limit=%d",
                productId,
                limit
            );

            log.debug("Calling ML API: {}", url);

            MlRecommendationResponse response = restTemplate.getForObject(
                url,
                MlRecommendationResponse.class
            );

            log.info(
                "ML API returned {} cross-sell products for {}",
                response != null ? response.getTotal() : 0,
                productId
            );

            return Optional.ofNullable(response);
        } catch (Exception e) {
            log.error(
                "Error calling ML API for product {}: {}",
                productId,
                e.getMessage()
            );
            return Optional.empty();
        }
    }
}
