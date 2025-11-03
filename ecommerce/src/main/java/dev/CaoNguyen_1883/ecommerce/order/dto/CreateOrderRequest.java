package dev.CaoNguyen_1883.ecommerce.order.dto;

import dev.CaoNguyen_1883.ecommerce.order.entity.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {

    @NotBlank(message = "Shipping address is required")
    @Size(max = 200, message = "Shipping address must not exceed 200 characters")
    private String shippingAddress;

    @NotBlank(message = "Shipping city is required")
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String shippingCity;

    @Size(max = 100, message = "District must not exceed 100 characters")
    private String shippingDistrict;

    @Size(max = 100, message = "Ward must not exceed 100 characters")
    private String shippingWard;

    @NotBlank(message = "Shipping phone is required")
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Phone must be 10-11 digits")
    private String shippingPhone;

    @NotBlank(message = "Recipient name is required")
    @Size(max = 100, message = "Recipient name must not exceed 100 characters")
    private String shippingRecipient;

    // Billing address (optional - if null, use shipping address)
    @Size(max = 200, message = "Billing address must not exceed 200 characters")
    private String billingAddress;

    @Size(max = 100, message = "City must not exceed 100 characters")
    private String billingCity;

    @Size(max = 100, message = "District must not exceed 100 characters")
    private String billingDistrict;

    @Size(max = 100, message = "Ward must not exceed 100 characters")
    private String billingWard;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
}