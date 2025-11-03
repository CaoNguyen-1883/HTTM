package dev.CaoNguyen_1883.ecommerce.order.entity;

public enum PaymentStatus {
    PENDING,        // Payment not yet made
    PAID,           // Payment completed
    FAILED,         // Payment failed
    REFUNDED        // Payment refunded
}