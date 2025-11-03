package dev.CaoNguyen_1883.ecommerce.cart.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemDto {
    private UUID id;
    private UUID variantId;
    private String variantSku;
    private String variantName;
    private String productName;
    private UUID productId;
    private String productSlug;
    private String primaryImage;
    private BigDecimal priceAtAdd;
    private Integer quantity;
    private BigDecimal subtotal;
    private Integer availableStock;
    private Boolean isAvailable; // Check if variant is still active and in stock
}