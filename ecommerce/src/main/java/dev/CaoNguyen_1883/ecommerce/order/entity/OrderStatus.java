package dev.CaoNguyen_1883.ecommerce.order.entity;

public enum OrderStatus {
    PENDING,        // Order created, waiting for payment/confirmation
    CONFIRMED,      // Order confirmed by admin/seller
    PROCESSING,     // Order is being prepared
    SHIPPED,        // Order shipped to customer
    DELIVERED,      // Order delivered successfully
    CANCELLED       // Order cancelled
}