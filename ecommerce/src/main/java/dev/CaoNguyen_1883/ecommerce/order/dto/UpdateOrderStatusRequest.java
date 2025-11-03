package dev.CaoNguyen_1883.ecommerce.order.dto;

import dev.CaoNguyen_1883.ecommerce.order.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateOrderStatusRequest {

    @NotNull(message = "Order status is required")
    private OrderStatus status;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
}