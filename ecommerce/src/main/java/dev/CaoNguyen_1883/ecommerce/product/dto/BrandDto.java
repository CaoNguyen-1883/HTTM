package dev.CaoNguyen_1883.ecommerce.product.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrandDto {
    private UUID id;
    private String name;
    private String description;
    private String slug;
    private String logoUrl;
    private String website;
    private String countryOfOrigin;
    private Boolean isFeatured;
    private LocalDateTime createdAt;
}