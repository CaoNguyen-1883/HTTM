package dev.CaoNguyen_1883.ecommerce.product.entity;

public enum ProductStatus {
    PENDING,      // Seller created, waiting for staff approval
    APPROVED,     // Staff approved, visible to customers
    REJECTED,     // Staff rejected
    ACTIVE,       // Currently selling
    INACTIVE,     // Temporarily not selling
    OUT_OF_STOCK  // All variants out of stock
}
