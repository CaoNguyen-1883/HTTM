package dev.CaoNguyen_1883.ecommerce.product.repository;

import dev.CaoNguyen_1883.ecommerce.product.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BrandRepository extends JpaRepository<Brand, UUID> {

    Optional<Brand> findBySlug(String slug);

    boolean existsByName(String name);

    boolean existsBySlug(String slug);

    @Query("SELECT b FROM Brand b WHERE b.isActive = true ORDER BY b.name")
    List<Brand> findAllActive();

    @Query("SELECT b FROM Brand b WHERE b.isFeatured = true AND b.isActive = true")
    List<Brand> findFeaturedBrands();
}
