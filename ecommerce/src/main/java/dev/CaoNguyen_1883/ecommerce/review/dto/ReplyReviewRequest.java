package dev.CaoNguyen_1883.ecommerce.review.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReplyReviewRequest {

    @NotBlank(message = "Reply content is required")
    @Size(min = 10, max = 1000, message = "Reply must be between 10 and 1000 characters")
    private String content;
}