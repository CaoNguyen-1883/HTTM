package dev.CaoNguyen_1883.ecommerce.review.dto;

import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewImageDto {
    private UUID id;
    private String imageUrl;
    private Integer displayOrder;
}