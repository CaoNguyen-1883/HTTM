package dev.CaoNguyen_1883.ecommerce.cart.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartDto {
    private UUID id;
    private UUID userId;
    private List<CartItemDto> items;
    private Integer totalItems;
    private BigDecimal subtotal;      // Sum of all item subtotals (before discount)
    private BigDecimal discount;      // Discount amount (from coupons/promotions)
    private BigDecimal totalPrice;    // subtotal - discount
    private LocalDateTime updatedAt;
}