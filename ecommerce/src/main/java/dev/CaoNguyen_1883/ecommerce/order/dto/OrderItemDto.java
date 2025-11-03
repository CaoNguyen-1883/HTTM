package dev.CaoNguyen_1883.ecommerce.order.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemDto {
    private UUID id;
    private UUID variantId;
    private String productName;
    private String variantSku;
    private String variantName;
    private String productImage;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal subtotal;
}