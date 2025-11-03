package dev.CaoNguyen_1883.ecommerce.recommendation.service.impl;

import dev.CaoNguyen_1883.ecommerce.cart.entity.Cart;
import dev.CaoNguyen_1883.ecommerce.cart.repository.CartRepository;
import dev.CaoNguyen_1883.ecommerce.common.exception.ResourceNotFoundException;
import dev.CaoNguyen_1883.ecommerce.product.dto.ProductSummaryDto;
import dev.CaoNguyen_1883.ecommerce.product.entity.Product;
import dev.CaoNguyen_1883.ecommerce.product.mapper.ProductMapper;
import dev.CaoNguyen_1883.ecommerce.product.repository.ProductRepository;
import dev.CaoNguyen_1883.ecommerce.recommendation.dto.RecommendationDto;
import dev.CaoNguyen_1883.ecommerce.recommendation.dto.RecommendationType;
import dev.CaoNguyen_1883.ecommerce.recommendation.service.IRecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RecommendationServiceImpl implements IRecommendationService {

    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final ProductMapper productMapper;

    @Override
    public List<RecommendationDto> getHomePageRecommendations() {
        log.info("Generating homepage recommendations");

        List<RecommendationDto> recommendations = new ArrayList<>();

        // Add trending products
        recommendations.add(getTrendingProducts(8));

        // Add best sellers
        recommendations.add(getBestSellingProducts(8));

        // Add new arrivals
        recommendations.add(getNewArrivals(8));

        // Add top rated
        recommendations.add(getTopRatedProducts(8));

        return recommendations;
    }

    @Override
    public RecommendationDto getTrendingProducts(int limit) {
        log.debug("Getting trending products, limit: {}", limit);

        Page<Product> products = productRepository.findTrendingProducts(
                PageRequest.of(0, limit)
        );

        return RecommendationDto.builder()
                .sectionTitle("Trending Products")
                .sectionDescription("Popular products right now")
                .type(RecommendationType.TRENDING)
                .products(products.getContent().stream()
                        .map(productMapper::toSummaryDto)
                        .collect(Collectors.toList()))
                .build();
    }

    @Override
    public RecommendationDto getBestSellingProducts(int limit) {
        log.debug("Getting best selling products, limit: {}", limit);

        Page<Product> products = productRepository.findBestSellingProducts(
                PageRequest.of(0, limit)
        );

        return RecommendationDto.builder()
                .sectionTitle("Best Sellers")
                .sectionDescription("Our most popular products")
                .type(RecommendationType.BEST_SELLERS)
                .products(products.getContent().stream()
                        .map(productMapper::toSummaryDto)
                        .collect(Collectors.toList()))
                .build();
    }

    @Override
    public RecommendationDto getTopRatedProducts(int limit) {
        log.debug("Getting top rated products, limit: {}", limit);

        Page<Product> products = productRepository.findTopRatedProducts(
                PageRequest.of(0, limit)
        );

        return RecommendationDto.builder()
                .sectionTitle("Top Rated")
                .sectionDescription("Highest rated by customers")
                .type(RecommendationType.TOP_RATED)
                .products(products.getContent().stream()
                        .map(productMapper::toSummaryDto)
                        .collect(Collectors.toList()))
                .build();
    }

    @Override
    public RecommendationDto getNewArrivals(int limit) {
        log.debug("Getting new arrivals, limit: {}", limit);

        Page<Product> products = productRepository.findNewArrivals(
                PageRequest.of(0, limit)
        );

        return RecommendationDto.builder()
                .sectionTitle("New Arrivals")
                .sectionDescription("Check out our latest products")
                .type(RecommendationType.NEW_ARRIVALS)
                .products(products.getContent().stream()
                        .map(productMapper::toSummaryDto)
                        .collect(Collectors.toList()))
                .build();
    }

    @Override
    public RecommendationDto getSimilarProducts(UUID productId, int limit) {
        log.debug("Getting similar products for product: {}, limit: {}", productId, limit);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        List<Product> similarProducts = productRepository.findSimilarProducts(
                product.getCategory().getId(),
                productId,
                PageRequest.of(0, limit)
        );

        return RecommendationDto.builder()
                .sectionTitle("Similar Products")
                .sectionDescription("You might also like")
                .type(RecommendationType.SIMILAR_PRODUCTS)
                .products(similarProducts.stream()
                        .map(productMapper::toSummaryDto)
                        .collect(Collectors.toList()))
                .build();
    }

    @Override
    public RecommendationDto getFrequentlyBoughtTogether(UUID productId, int limit) {
        log.debug("Getting frequently bought together for product: {}, limit: {}", productId, limit);

        List<Product> products = productRepository.findFrequentlyBoughtTogether(
                productId,
                PageRequest.of(0, limit)
        );

        // If no co-purchased products found, fall back to similar products
        if (products.isEmpty()) {
            log.debug("No frequently bought together found, falling back to similar products");
            return getSimilarProducts(productId, limit);
        }

        return RecommendationDto.builder()
                .sectionTitle("Frequently Bought Together")
                .sectionDescription("Customers also bought these items")
                .type(RecommendationType.FREQUENTLY_BOUGHT_TOGETHER)
                .products(products.stream()
                        .map(productMapper::toSummaryDto)
                        .collect(Collectors.toList()))
                .build();
    }

    @Override
    public RecommendationDto getRecommendationsBasedOnCart(UUID userId, int limit) {
        log.debug("Getting recommendations based on cart for user: {}, limit: {}", userId, limit);

        Optional<Cart> cartOpt = cartRepository.findByUserIdWithItems(userId);

        if (cartOpt.isEmpty() || cartOpt.get().getItems().isEmpty()) {
            // No cart or empty cart - return trending products
            return getTrendingProducts(limit);
        }

        Cart cart = cartOpt.get();

        // Get all unique product IDs from cart items
        Set<UUID> cartProductIds = cart.getItems().stream()
                .map(item -> item.getVariant().getProduct().getId())
                .collect(Collectors.toSet());

        // Get all unique category IDs from cart products
        Set<UUID> categoryIds = cart.getItems().stream()
                .map(item -> item.getVariant().getProduct().getCategory().getId())
                .collect(Collectors.toSet());

        // Find similar products from same categories (excluding cart products)
        List<Product> recommendations = new ArrayList<>();

        for (UUID categoryId : categoryIds) {
            Page<Product> categoryProducts = productRepository.findByCategoryId(
                    categoryId,
                    PageRequest.of(0, limit)
            );

            // Filter out products already in cart
            categoryProducts.getContent().stream()
                    .filter(p -> !cartProductIds.contains(p.getId()))
                    .forEach(recommendations::add);
        }

        // Remove duplicates and limit
        List<Product> uniqueRecommendations = recommendations.stream()
                .distinct()
                .limit(limit)
                .collect(Collectors.toList());

        // If not enough recommendations, add trending products
        if (uniqueRecommendations.size() < limit) {
            Page<Product> trending = productRepository.findTrendingProducts(
                    PageRequest.of(0, limit - uniqueRecommendations.size())
            );

            trending.getContent().stream()
                    .filter(p -> !cartProductIds.contains(p.getId()))
                    .filter(p -> !uniqueRecommendations.contains(p))
                    .forEach(uniqueRecommendations::add);
        }

        return RecommendationDto.builder()
                .sectionTitle("Recommended For You")
                .sectionDescription("Based on items in your cart")
                .type(RecommendationType.FOR_YOU)
                .products(uniqueRecommendations.stream()
                        .map(productMapper::toSummaryDto)
                        .collect(Collectors.toList()))
                .build();
    }
}
