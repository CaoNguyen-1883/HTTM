package dev.CaoNguyen_1883.ecommerce.product.service;

import dev.CaoNguyen_1883.ecommerce.product.dto.ProductDto;
import dev.CaoNguyen_1883.ecommerce.product.dto.ProductRequest;
import dev.CaoNguyen_1883.ecommerce.product.dto.ProductSummaryDto;
import dev.CaoNguyen_1883.ecommerce.product.dto.ProductUpdateRequest;
import dev.CaoNguyen_1883.ecommerce.product.entity.ProductStatus;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface IProductService {
    Page<ProductSummaryDto> getAllProducts(Pageable pageable);

    Page<ProductSummaryDto> filterProducts(String keyword, UUID categoryId, UUID brandId,
                                          Double minPrice, Double maxPrice, Pageable pageable);

    Page<ProductSummaryDto> getAllProductsForAdmin(Pageable pageable);

    Page<ProductSummaryDto> getAllProductsForAdminWithKeyword(String keyword, Pageable pageable);

    Page<ProductSummaryDto> getProductsByStatus(ProductStatus status, Pageable pageable);

    Page<ProductSummaryDto> getProductsByStatusAndKeyword(ProductStatus status, String keyword, Pageable pageable);

    Page<ProductSummaryDto> getPendingProducts(Pageable pageable);

    Page<ProductSummaryDto> getSellerProducts(UUID sellerId, Pageable pageable);

    Page<ProductSummaryDto> searchProducts(String keyword, Pageable pageable);

    Page<ProductSummaryDto> getProductsByCategory(UUID categoryId, Pageable pageable);

    Page<ProductSummaryDto> getProductsByBrand(UUID brandId, Pageable pageable);

    @Cacheable(value = "products", key = "#id")
    ProductDto getProductById(UUID id);

    @Cacheable(value = "products", key = "'slug_' + #slug")
    ProductDto getProductBySlug(String slug);

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    ProductDto createProduct(ProductRequest request, UUID sellerId);

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    ProductDto updateProduct(UUID id, ProductUpdateRequest request, UUID sellerId);

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    void deleteProduct(UUID id, UUID sellerId);

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    ProductDto approveProduct(UUID id, UUID approvedBy);

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    ProductDto rejectProduct(UUID id, String reason, UUID rejectedBy);

    Page<ProductSummaryDto> getTrendingProducts(Pageable pageable);

    Page<ProductSummaryDto> getBestSellingProducts(Pageable pageable);

    Page<ProductSummaryDto> getTopRatedProducts(Pageable pageable);

    List<ProductSummaryDto> getSimilarProducts(UUID productId, int limit);

    @Transactional
    void incrementViewCount(UUID productId);
}
