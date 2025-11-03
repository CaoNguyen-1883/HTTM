package dev.CaoNguyen_1883.ecommerce.product.service;

import dev.CaoNguyen_1883.ecommerce.product.dto.BrandDto;
import dev.CaoNguyen_1883.ecommerce.product.dto.BrandRequest;

import java.util.List;
import java.util.UUID;

public interface IBrandService {

    /**
     * Get all active brands
     */
    List<BrandDto> getAllBrands();

    /**
     * Get featured brands
     */
    List<BrandDto> getFeaturedBrands();

    /**
     * Get brand by ID
     */
    BrandDto getBrandById(UUID id);

    /**
     * Get brand by slug
     */
    BrandDto getBrandBySlug(String slug);

    /**
     * Create new brand
     */
    BrandDto createBrand(BrandRequest request);

    /**
     * Update brand
     */
    BrandDto updateBrand(UUID id, BrandRequest request);

    /**
     * Delete brand (soft delete)
     */
    void deleteBrand(UUID id);

    /**
     * Restore brand
     */
    BrandDto restoreBrand(UUID id);
}
