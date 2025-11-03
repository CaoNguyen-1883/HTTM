package dev.CaoNguyen_1883.ecommerce.order.repository;

import dev.CaoNguyen_1883.ecommerce.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    // Find items by order
    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.id = :orderId")
    List<OrderItem> findByOrderId(@Param("orderId") UUID orderId);

    // Find items by variant (for analytics)
    @Query("SELECT oi FROM OrderItem oi WHERE oi.variant.id = :variantId")
    List<OrderItem> findByVariantId(@Param("variantId") UUID variantId);

    // Count items by variant
    @Query("SELECT COUNT(oi) FROM OrderItem oi WHERE oi.variant.id = :variantId")
    long countByVariantId(@Param("variantId") UUID variantId);

    // Get total quantity sold for a variant
    @Query("SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi " +
            "WHERE oi.variant.id = :variantId " +
            "AND oi.order.orderStatus = 'DELIVERED'")
    Long getTotalQuantitySoldByVariantId(@Param("variantId") UUID variantId);
}