package dev.CaoNguyen_1883.ecommerce.order.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatistics {
    private Long totalOrders;
    private Long pendingOrders;
    private Long confirmedOrders;
    private Long processingOrders;
    private Long shippedOrders;
    private Long deliveredOrders;
    private Long cancelledOrders;
    private Double totalRevenue;
}