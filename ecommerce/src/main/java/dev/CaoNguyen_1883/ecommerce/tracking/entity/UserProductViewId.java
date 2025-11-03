package dev.CaoNguyen_1883.ecommerce.tracking.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

/**
 * Composite key for UserProductView entity
 * Represents the combination of user_id and product_id
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProductViewId implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID userId;
    private UUID productId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserProductViewId that = (UserProductViewId) o;

        if (!userId.equals(that.userId)) return false;
        return productId.equals(that.productId);
    }

    @Override
    public int hashCode() {
        int result = userId.hashCode();
        result = 31 * result + productId.hashCode();
        return result;
    }
}
