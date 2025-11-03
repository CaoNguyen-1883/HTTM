package dev.CaoNguyen_1883.ecommerce.product.entity;

import dev.CaoNguyen_1883.ecommerce.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "brands")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Brand extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(unique = true, nullable = false, length = 150)
    private String slug;

    private String logoUrl;

    private String website;

    @Column(length = 50)
    private String countryOfOrigin;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isFeatured = false;  // For homepage display
}
