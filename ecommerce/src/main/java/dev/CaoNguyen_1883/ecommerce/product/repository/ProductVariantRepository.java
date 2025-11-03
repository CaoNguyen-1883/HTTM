package dev.CaoNguyen_1883.ecommerce.product.repository;

import dev.CaoNguyen_1883.ecommerce.product.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, UUID> {

    Optional<ProductVariant> findBySku(String sku);

    boolean existsBySku(String sku);

    @Query("SELECT v FROM ProductVariant v WHERE v.product.id = :productId")
    List<ProductVariant> findByProductId(@Param("productId") UUID productId);

    @Query("SELECT v FROM ProductVariant v " +
            "WHERE v.product.id = :productId AND v.isDefault = true")
    Optional<ProductVariant> findDefaultVariant(@Param("productId") UUID productId);

    @Query("SELECT v FROM ProductVariant v " +
            "WHERE v.product.id = :productId AND v.stock > 0")
    List<ProductVariant> findAvailableVariants(@Param("productId") UUID productId);

    @Query("SELECT v FROM ProductVariant v " +
            "LEFT JOIN FETCH v.product " +
            "WHERE v.id = :id")
    Optional<ProductVariant> findByIdWithProduct(@Param("id") UUID variantId);
}

