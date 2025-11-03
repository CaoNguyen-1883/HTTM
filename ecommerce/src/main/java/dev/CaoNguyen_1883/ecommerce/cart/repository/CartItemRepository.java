package dev.CaoNguyen_1883.ecommerce.cart.repository;

import dev.CaoNguyen_1883.ecommerce.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    // Find item by cart and variant
    @Query("SELECT ci FROM CartItem ci " +
            "WHERE ci.cart.id = :cartId " +
            "AND ci.variant.id = :variantId " +
            "AND ci.isActive = true")
    Optional<CartItem> findByCartIdAndVariantId(
            @Param("cartId") UUID cartId,
            @Param("variantId") UUID variantId
    );

    // Check if item exists in cart
    boolean existsByCartIdAndVariantIdAndIsActiveTrue(UUID cartId, UUID variantId);

    // Delete all items in cart
    @Modifying
    @Query("UPDATE CartItem ci SET ci.isActive = false WHERE ci.cart.id = :cartId")
    void softDeleteAllByCartId(@Param("cartId") UUID cartId);

    // Delete specific item
    @Modifying
    @Query("UPDATE CartItem ci SET ci.isActive = false WHERE ci.id = :itemId")
    void softDeleteById(@Param("itemId") UUID itemId);

    // Count items in cart
    @Query("SELECT COUNT(ci) FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.isActive = true")
    long countByCartId(@Param("cartId") UUID cartId);
}