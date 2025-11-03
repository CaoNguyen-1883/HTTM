package dev.CaoNguyen_1883.ecommerce.review.entity;

import dev.CaoNguyen_1883.ecommerce.common.entity.BaseEntity;
import dev.CaoNguyen_1883.ecommerce.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "review_helpfulness",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_review_user_helpfulness", columnNames = {"review_id", "user_id"})
    },
    indexes = {
        @Index(name = "idx_helpfulness_review", columnList = "review_id"),
        @Index(name = "idx_helpfulness_user", columnList = "user_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ReviewHelpfulness extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Boolean isHelpful;  // true = helpful, false = not helpful
}