package dev.CaoNguyen_1883.ecommerce.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductApprovalRequest {
    @NotBlank(message = "Approval status is required (APPROVED or REJECTED)")
    @Schema(description = "Approval decision", example = "APPROVED")
    private String status;  // APPROVED or REJECTED

    @Schema(description = "Rejection reason (required if rejected)")
    private String reason;
}
