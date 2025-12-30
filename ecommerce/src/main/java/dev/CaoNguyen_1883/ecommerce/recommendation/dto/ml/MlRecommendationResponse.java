package dev.CaoNguyen_1883.ecommerce.recommendation.dto.ml;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MlRecommendationResponse {
    private List<MlRecommendationItem> recommendations;
    private Integer total;
    private String source;
}
