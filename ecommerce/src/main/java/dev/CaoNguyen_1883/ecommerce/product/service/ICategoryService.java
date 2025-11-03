package dev.CaoNguyen_1883.ecommerce.product.service;


import dev.CaoNguyen_1883.ecommerce.product.dto.CategoryDto;
import dev.CaoNguyen_1883.ecommerce.product.dto.CategoryRequest;

import java.util.List;
import java.util.UUID;

public interface ICategoryService {

    /**
     * Get all active categories
     */
    List<CategoryDto> getAllCategories();

    /**
     * Get root categories (no parent)
     */
    List<CategoryDto> getRootCategories();

    /**
     * Get categories by parent ID
     */
    List<CategoryDto> getCategoriesByParentId(UUID parentId);

    /**
     * Get category by ID
     */
    CategoryDto getCategoryById(UUID id);

    /**
     * Get category by slug
     */
    CategoryDto getCategoryBySlug(String slug);

    /**
     * Create new category
     */
    CategoryDto createCategory(CategoryRequest request);

    /**
     * Update category
     */
    CategoryDto updateCategory(UUID id, CategoryRequest request);

    /**
     * Delete category (soft delete)
     */
    void deleteCategory(UUID id);

    /**
     * Restore category
     */
    CategoryDto restoreCategory(UUID id);
}
