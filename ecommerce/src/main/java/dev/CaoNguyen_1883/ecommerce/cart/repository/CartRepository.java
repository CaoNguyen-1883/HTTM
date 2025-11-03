package dev.CaoNguyen_1883.ecommerce.cart.repository;

import dev.CaoNguyen_1883.ecommerce.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {

    // Find cart by user ID
    @Query("SELECT c FROM Cart c WHERE c.user.id = :userId AND c.isActive = true")
    Optional<Cart> findByUserId(@Param("userId") UUID userId);

    // Find cart with all items and variant details
    @Query("SELECT DISTINCT c FROM Cart c " +
            "LEFT JOIN FETCH c.items ci " +
            "LEFT JOIN FETCH ci.variant v " +
            "LEFT JOIN FETCH v.product p " +
            "LEFT JOIN FETCH v.images " +
            "WHERE c.user.id = :userId AND c.isActive = true")
    Optional<Cart> findByUserIdWithItems(@Param("userId") UUID userId);

    // Check if cart exists for user
    boolean existsByUserIdAndIsActiveTrue(UUID userId);

    // Delete cart by user ID (soft delete through isActive)
    @Query("UPDATE Cart c SET c.isActive = false WHERE c.user.id = :userId")
    void softDeleteByUserId(@Param("userId") UUID userId);
}