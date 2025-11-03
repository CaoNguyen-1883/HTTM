package dev.CaoNguyen_1883.ecommerce.order.entity;

public enum PaymentMethod {
    COD,            // Cash on delivery
    BANK_TRANSFER,  // Bank transfer
    CREDIT_CARD,    // Credit card
    DEBIT_CARD,     // Debit card
    E_WALLET        // E-wallet (Momo, ZaloPay, etc.)
}