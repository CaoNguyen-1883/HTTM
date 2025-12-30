package dev.CaoNguyen_1883.ecommerce.order.repository;

import dev.CaoNguyen_1883.ecommerce.order.entity.Order;
import dev.CaoNguyen_1883.ecommerce.order.entity.OrderStatus;
import dev.CaoNguyen_1883.ecommerce.order.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    // Find by order number
    Optional<Order> findByOrderNumber(String orderNumber);

    boolean existsByOrderNumber(String orderNumber);

    // Find order with all items and relationships
    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.items oi " +
            "LEFT JOIN FETCH oi.variant v " +
            "LEFT JOIN FETCH o.user u " +
            "WHERE o.id = :id")
    Optional<Order> findByIdWithDetails(@Param("id") UUID id);

    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.items oi " +
            "LEFT JOIN FETCH oi.variant v " +
            "LEFT JOIN FETCH o.user u " +
            "WHERE o.orderNumber = :orderNumber")
    Optional<Order> findByOrderNumberWithDetails(@Param("orderNumber") String orderNumber);

    // Find orders by user
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId ORDER BY o.createdAt DESC")
    Page<Order> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    // Find orders by status
    @Query("SELECT o FROM Order o WHERE o.orderStatus = :status ORDER BY o.createdAt DESC")
    Page<Order> findByOrderStatus(@Param("status") OrderStatus status, Pageable pageable);

    // Find orders by payment status
    @Query("SELECT o FROM Order o WHERE o.paymentStatus = :status ORDER BY o.createdAt DESC")
    Page<Order> findByPaymentStatus(@Param("status") PaymentStatus status, Pageable pageable);

    // Find pending orders (for admin)
    @Query("SELECT o FROM Order o " +
            "WHERE o.orderStatus = 'PENDING' " +
            "ORDER BY o.createdAt ASC")
    Page<Order> findPendingOrders(Pageable pageable);

    // Find orders by date range
    @Query("SELECT o FROM Order o " +
            "WHERE o.createdAt BETWEEN :startDate AND :endDate " +
            "ORDER BY o.createdAt DESC")
    Page<Order> findByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    // Search orders
    @Query("SELECT o FROM Order o " +
            "WHERE (o.orderNumber LIKE %:keyword% " +
            "OR o.user.email LIKE %:keyword% " +
            "OR o.user.fullName LIKE %:keyword% " +
            "OR o.shippingPhone LIKE %:keyword% " +
            "OR o.shippingRecipient LIKE %:keyword%) " +
            "ORDER BY o.createdAt DESC")
    Page<Order> searchOrders(@Param("keyword") String keyword, Pageable pageable);

    // Count orders by status
    long countByOrderStatus(OrderStatus status);

    // Count orders by user
    long countByUserId(UUID userId);

    // Get total revenue
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o " +
            "WHERE o.orderStatus = 'DELIVERED' " +
            "AND o.paymentStatus = 'PAID'")
    Double getTotalRevenue();

    // Get total revenue by date range
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o " +
            "WHERE o.orderStatus = 'DELIVERED' " +
            "AND o.paymentStatus = 'PAID' " +
            "AND o.deliveredAt BETWEEN :startDate AND :endDate")
    Double getTotalRevenueByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // Get user's latest orders
    @Query("SELECT o FROM Order o " +
            "WHERE o.user.id = :userId " +
            "ORDER BY o.createdAt DESC")
    List<Order> findLatestOrdersByUserId(@Param("userId") UUID userId, Pageable pageable);


    List<Order> findByUserIdOrderByCreatedAtDesc(UUID userId);
}