package dev.CaoNguyen_1883.ecommerce.product.dto;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariantDto {
    private UUID id;
    private String sku;
    private String name;
    private BigDecimal price;
    private Integer stock;
    private Integer availableStock;
    private Map<String, Object> specifications;
    private Map<String, Object> attributes;
    private Boolean isDefault;
    private List<ProductImageDto> images;
}