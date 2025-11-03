package dev.CaoNguyen_1883.ecommerce.review.repository;

import dev.CaoNguyen_1883.ecommerce.review.entity.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReviewImageRepository extends JpaRepository<ReviewImage, UUID> {

    // Find images by review
    @Query("SELECT ri FROM ReviewImage ri " +
            "WHERE ri.review.id = :reviewId " +
            "AND ri.isActive = true " +
            "ORDER BY ri.displayOrder ASC")
    List<ReviewImage> findByReviewId(@Param("reviewId") UUID reviewId);

    // Delete all images by review
    @Modifying
    @Query("UPDATE ReviewImage ri SET ri.isActive = false WHERE ri.review.id = :reviewId")
    void softDeleteByReviewId(@Param("reviewId") UUID reviewId);
}