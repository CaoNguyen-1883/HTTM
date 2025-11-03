package dev.CaoNguyen_1883.ecommerce.review.entity;

public enum ReviewStatus {
    PENDING,    // Waiting for moderation
    APPROVED,   // Approved and visible
    REJECTED    // Rejected and hidden
}