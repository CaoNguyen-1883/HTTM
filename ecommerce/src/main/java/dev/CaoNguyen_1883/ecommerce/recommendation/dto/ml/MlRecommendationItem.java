package dev.CaoNguyen_1883.ecommerce.recommendation.dto.ml;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MlRecommendationItem {

    @JsonProperty("product_id")
    private String productId;  // String because Python returns UUID with dashes

    private Double score;

    @JsonProperty("recommendation_type")
    private String recommendationType;

    /**
     * Helper method to convert product_id string to UUID
     * @return UUID object
     */
    public UUID getProductUuid() {
        return UUID.fromString(productId);
    }
}
