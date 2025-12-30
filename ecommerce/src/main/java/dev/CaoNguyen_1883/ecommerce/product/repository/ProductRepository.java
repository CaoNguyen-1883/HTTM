package dev.CaoNguyen_1883.ecommerce.product.repository;

import dev.CaoNguyen_1883.ecommerce.product.entity.Product;
import dev.CaoNguyen_1883.ecommerce.product.entity.ProductStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    Optional<Product> findBySlug(String slug);

    boolean existsBySlug(String slug);

    // Find with all relationships
    @Query(
        "SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN FETCH p.category " +
            "LEFT JOIN FETCH p.brand " +
            "LEFT JOIN FETCH p.seller " +
            "LEFT JOIN FETCH p.variants v " +
            "LEFT JOIN FETCH v.images " +
            "WHERE p.id = :id"
    )
    Optional<Product> findByIdWithDetails(@Param("id") UUID id);

    // Products by status
    @Query(
        "SELECT p FROM Product p WHERE p.status = :status AND p.isActive = true"
    )
    Page<Product> findByStatus(
        @Param("status") ProductStatus status,
        Pageable pageable
    );

    // Products by status with keyword search
    @Query(
        "SELECT p FROM Product p " +
            "WHERE p.status = :status AND p.isActive = true " +
            "AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.shortDescription) LIKE LOWER(CONCAT('%', :keyword, '%')))"
    )
    Page<Product> findByStatusAndKeyword(
        @Param("status") ProductStatus status,
        @Param("keyword") String keyword,
        Pageable pageable
    );

    // Approved products for customers
    @Query(
        "SELECT p FROM Product p " +
            "WHERE p.status = 'APPROVED' AND p.isActive = true"
    )
    Page<Product> findApprovedProducts(Pageable pageable);

    // Search products
    @Query(
        "SELECT p FROM Product p " +
            "WHERE (LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.shortDescription) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND p.status = 'APPROVED' AND p.isActive = true"
    )
    Page<Product> searchProducts(
        @Param("keyword") String keyword,
        Pageable pageable
    );

    // Filter by category
    @Query(
        "SELECT p FROM Product p " +
            "WHERE p.category.id = :categoryId " +
            "AND p.status = 'APPROVED' AND p.isActive = true"
    )
    Page<Product> findByCategoryId(
        @Param("categoryId") UUID categoryId,
        Pageable pageable
    );

    // Filter by brand
    @Query(
        "SELECT p FROM Product p " +
            "WHERE p.brand.id = :brandId " +
            "AND p.status = 'APPROVED' AND p.isActive = true"
    )
    Page<Product> findByBrandId(
        @Param("brandId") UUID brandId,
        Pageable pageable
    );

    // Filter products with multiple criteria
    @Query(
        "SELECT p FROM Product p " +
            "WHERE p.status = 'APPROVED' AND p.isActive = true " +
            "AND (:keyword IS NULL OR " +
            "     LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "     LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "     LOWER(p.shortDescription) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
            "AND (:brandId IS NULL OR p.brand.id = :brandId) " +
            "AND (:minPrice IS NULL OR p.basePrice >= :minPrice) " +
            "AND (:maxPrice IS NULL OR p.basePrice <= :maxPrice)"
    )
    Page<Product> filterProducts(
        @Param("keyword") String keyword,
        @Param("categoryId") UUID categoryId,
        @Param("brandId") UUID brandId,
        @Param("minPrice") Double minPrice,
        @Param("maxPrice") Double maxPrice,
        Pageable pageable
    );

    // Filter by seller
    @Query("SELECT p FROM Product p WHERE p.seller.id = :sellerId")
    Page<Product> findBySellerId(
        @Param("sellerId") UUID sellerId,
        Pageable pageable
    );

    // Filter by seller and keyword
    @Query(
        "SELECT p FROM Product p " +
            "WHERE p.seller.id = :sellerId " +
            "AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.shortDescription) LIKE LOWER(CONCAT('%', :keyword, '%')))"
    )
    Page<Product> findBySellerIdAndNameContainingIgnoreCase(
        @Param("sellerId") UUID sellerId,
        @Param("keyword") String keyword,
        Pageable pageable
    );

    // Filter by seller and status
    @Query(
        "SELECT p FROM Product p WHERE p.seller.id = :sellerId AND p.status = :status"
    )
    Page<Product> findBySellerIdAndStatus(
        @Param("sellerId") UUID sellerId,
        @Param("status") ProductStatus status,
        Pageable pageable
    );

    // Filter by seller, status and keyword
    @Query(
        "SELECT p FROM Product p " +
            "WHERE p.seller.id = :sellerId " +
            "AND p.status = :status " +
            "AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.shortDescription) LIKE LOWER(CONCAT('%', :keyword, '%')))"
    )
    Page<Product> findBySellerIdAndStatusAndNameContainingIgnoreCase(
        @Param("sellerId") UUID sellerId,
        @Param("status") ProductStatus status,
        @Param("keyword") String keyword,
        Pageable pageable
    );

    // Get all products for admin/staff (all statuses)
    @Query("SELECT p FROM Product p WHERE p.isActive = true")
    Page<Product> findAllForAdmin(Pageable pageable);

    // Get all products for admin/staff with keyword search
    @Query(
        "SELECT p FROM Product p " +
            "WHERE p.isActive = true " +
            "AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.shortDescription) LIKE LOWER(CONCAT('%', :keyword, '%')))"
    )
    Page<Product> findAllForAdminWithKeyword(
        @Param("keyword") String keyword,
        Pageable pageable
    );

    // Trending products (by view count)
    @Query(
        "SELECT p FROM Product p " +
            "WHERE p.status = 'APPROVED' AND p.isActive = true " +
            "ORDER BY p.viewCount DESC"
    )
    Page<Product> findTrendingProducts(Pageable pageable);

    // Best selling products
    @Query(
        "SELECT p FROM Product p " +
            "WHERE p.status = 'APPROVED' AND p.isActive = true " +
            "ORDER BY p.purchaseCount DESC"
    )
    Page<Product> findBestSellingProducts(Pageable pageable);

    // Top rated products
    @Query(
        "SELECT p FROM Product p " +
            "WHERE p.status = 'APPROVED' AND p.isActive = true " +
            "AND p.averageRating IS NOT NULL " +
            "ORDER BY p.averageRating DESC, p.totalReviews DESC"
    )
    Page<Product> findTopRatedProducts(Pageable pageable);

    // Similar products (same category, different product)
    @Query(
        "SELECT p FROM Product p " +
            "WHERE p.category.id = :categoryId " +
            "AND p.id != :productId " +
            "AND p.status = 'APPROVED' AND p.isActive = true"
    )
    List<Product> findSimilarProducts(
        @Param("categoryId") UUID categoryId,
        @Param("productId") UUID productId,
        Pageable pageable
    );

    // Frequently bought together (based on orders)
    @Query(
        "SELECT p FROM Product p " +
            "WHERE p.id IN (" +
            "  SELECT oi2.variant.product.id FROM OrderItem oi1 " +
            "  JOIN OrderItem oi2 ON oi1.order.id = oi2.order.id " +
            "  WHERE oi1.variant.product.id = :productId " +
            "  AND oi2.variant.product.id != :productId " +
            "  GROUP BY oi2.variant.product.id " +
            "  ORDER BY COUNT(*) DESC" +
            ") " +
            "AND p.status = 'APPROVED' AND p.isActive = true"
    )
    List<Product> findFrequentlyBoughtTogether(
        @Param("productId") UUID productId,
        Pageable pageable
    );

    // New arrivals (newest products)
    @Query(
        "SELECT p FROM Product p " +
            "WHERE p.status = 'APPROVED' AND p.isActive = true " +
            "ORDER BY p.createdAt DESC"
    )
    Page<Product> findNewArrivals(Pageable pageable);

    // Get products by IDs (for cart-based recommendations)
    @Query(
        "SELECT p FROM Product p " +
            "WHERE p.id IN :productIds " +
            "AND p.status = 'APPROVED' AND p.isActive = true"
    )
    List<Product> findByIdIn(@Param("productIds") List<UUID> productIds);
}
