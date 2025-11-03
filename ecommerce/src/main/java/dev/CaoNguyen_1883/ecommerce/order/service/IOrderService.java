package dev.CaoNguyen_1883.ecommerce.order.service;

import dev.CaoNguyen_1883.ecommerce.order.dto.*;
import dev.CaoNguyen_1883.ecommerce.order.entity.OrderStatus;
import dev.CaoNguyen_1883.ecommerce.order.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface IOrderService {

    /**
     * Create order from user's cart
     */
    @Transactional
    OrderDto createOrderFromCart(UUID userId, CreateOrderRequest request);

    /**
     * Get order by ID
     */
    OrderDto getOrderById(UUID orderId);

    /**
     * Get order by order number
     */
    OrderDto getOrderByOrderNumber(String orderNumber);

    /**
     * Get user's orders
     */
    Page<OrderDto> getUserOrders(UUID userId, Pageable pageable);

    /**
     * Get all orders (admin/staff)
     */
    Page<OrderDto> getAllOrders(Pageable pageable);

    /**
     * Get orders by status
     */
    Page<OrderDto> getOrdersByStatus(OrderStatus status, Pageable pageable);

    /**
     * Get orders by payment status
     */
    Page<OrderDto> getOrdersByPaymentStatus(PaymentStatus status, Pageable pageable);

    /**
     * Get pending orders
     */
    Page<OrderDto> getPendingOrders(Pageable pageable);

    /**
     * Search orders
     */
    Page<OrderDto> searchOrders(String keyword, Pageable pageable);

    /**
     * Get orders by date range
     */
    Page<OrderDto> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Confirm order (admin/seller)
     */
    @Transactional
    OrderDto confirmOrder(UUID orderId, UUID confirmedBy);

    /**
     * Mark order as processing
     */
    @Transactional
    OrderDto markAsProcessing(UUID orderId);

    /**
     * Ship order
     */
    @Transactional
    OrderDto shipOrder(UUID orderId);

    /**
     * Deliver order
     */
    @Transactional
    OrderDto deliverOrder(UUID orderId);

    /**
     * Cancel order
     */
    @Transactional
    OrderDto cancelOrder(UUID orderId, CancelOrderRequest request, UUID cancelledBy);

    /**
     * Update order status (admin)
     */
    @Transactional
    OrderDto updateOrderStatus(UUID orderId, UpdateOrderStatusRequest request, UUID updatedBy);

    /**
     * Mark payment as paid
     */
    @Transactional
    OrderDto markPaymentAsPaid(UUID orderId);

    /**
     * Mark payment as failed
     */
    @Transactional
    OrderDto markPaymentAsFailed(UUID orderId);

    /**
     * Get order statistics
     */
    OrderStatistics getOrderStatistics();

    /**
     * Get total revenue
     */
    Double getTotalRevenue();

    /**
     * Get revenue by date range
     */
    Double getRevenueByDateRange(LocalDateTime startDate, LocalDateTime endDate);
}