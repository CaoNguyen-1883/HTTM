package dev.CaoNguyen_1883.ecommerce.user.entity;

import dev.CaoNguyen_1883.ecommerce.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "permissions", indexes = @Index(name = "idx_permission_name", columnList = "name"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Permission extends BaseEntity {
    @Column(unique = true, nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String description;
}
