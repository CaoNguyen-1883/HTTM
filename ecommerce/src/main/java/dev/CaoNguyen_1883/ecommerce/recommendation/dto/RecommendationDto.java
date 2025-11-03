package dev.CaoNguyen_1883.ecommerce.recommendation.dto;

import dev.CaoNguyen_1883.ecommerce.product.dto.ProductSummaryDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationDto {
    private String sectionTitle;
    private String sectionDescription;
    private RecommendationType type;
    private List<ProductSummaryDto> products;
}
