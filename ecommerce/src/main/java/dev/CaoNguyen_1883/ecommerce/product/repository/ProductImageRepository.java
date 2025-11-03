package dev.CaoNguyen_1883.ecommerce.product.repository;

import dev.CaoNguyen_1883.ecommerce.product.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, UUID> {

    @Query("SELECT i FROM ProductImage i " +
            "WHERE i.product.id = :productId " +
            "ORDER BY i.displayOrder")
    List<ProductImage> findByProductId(@Param("productId") UUID productId);

    @Query("SELECT i FROM ProductImage i " +
            "WHERE i.variant.id = :variantId " +
            "ORDER BY i.displayOrder")
    List<ProductImage> findByVariantId(@Param("variantId") UUID variantId);

    @Query("SELECT i FROM ProductImage i " +
            "WHERE i.product.id = :productId AND i.isPrimary = true")
    Optional<ProductImage> findPrimaryImageByProductId(@Param("productId") UUID productId);

    @Query("SELECT i FROM ProductImage i " +
            "WHERE i.variant.id = :variantId AND i.isPrimary = true")
    Optional<ProductImage> findPrimaryImageByVariantId(@Param("variantId") UUID variantId);
}