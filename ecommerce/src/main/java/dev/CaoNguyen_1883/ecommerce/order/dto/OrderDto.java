package dev.CaoNguyen_1883.ecommerce.order.dto;

import dev.CaoNguyen_1883.ecommerce.order.entity.OrderStatus;
import dev.CaoNguyen_1883.ecommerce.order.entity.PaymentMethod;
import dev.CaoNguyen_1883.ecommerce.order.entity.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDto {
    private UUID id;
    private String orderNumber;
    private UUID userId;
    private String userEmail;
    private String userFullName;
    private List<OrderItemDto> items;
    
    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal tax;
    private BigDecimal discount;
    private BigDecimal totalAmount;
    
    private String shippingAddress;
    private String shippingCity;
    private String shippingDistrict;
    private String shippingWard;
    private String shippingPhone;
    private String shippingRecipient;
    
    private String billingAddress;
    private String billingCity;
    private String billingDistrict;
    private String billingWard;
    
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private OrderStatus orderStatus;
    
    private String notes;
    private String cancelReason;
    private String adminNotes;
    
    private LocalDateTime confirmedAt;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime cancelledAt;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}