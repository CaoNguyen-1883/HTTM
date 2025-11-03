package dev.CaoNguyen_1883.ecommerce.product.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDto {
    private UUID id;
    private String name;
    private String description;
    private String slug;
    private String imageUrl;
    private UUID parentId;
    private String parentName;
    private List<CategoryDto> children;
    private Integer displayOrder;
    private LocalDateTime createdAt;
}
