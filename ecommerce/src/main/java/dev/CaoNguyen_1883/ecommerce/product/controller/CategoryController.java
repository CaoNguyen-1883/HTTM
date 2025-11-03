package dev.CaoNguyen_1883.ecommerce.product.controller;

import dev.CaoNguyen_1883.ecommerce.common.response.ApiResponse;
import dev.CaoNguyen_1883.ecommerce.product.dto.CategoryDto;
import dev.CaoNguyen_1883.ecommerce.product.dto.CategoryRequest;
import dev.CaoNguyen_1883.ecommerce.product.service.ICategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Category Management", description = "APIs for managing product categories")
public class CategoryController {

    private final ICategoryService categoryService;

    @Operation(
            summary = "Get all categories",
            description = "Retrieve all active categories (Public - no authentication required)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Categories retrieved successfully"
            )
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryDto>>> getAllCategories() {
        List<CategoryDto> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success("Categories retrieved successfully", categories));
    }

    @Operation(
            summary = "Get root categories",
            description = "Retrieve root categories with their children (Public)"
    )
    @GetMapping("/root")
    public ResponseEntity<ApiResponse<List<CategoryDto>>> getRootCategories() {
        List<CategoryDto> categories = categoryService.getRootCategories();
        return ResponseEntity.ok(ApiResponse.success("Root categories retrieved successfully", categories));
    }

    @Operation(
            summary = "Get category by ID",
            description = "Retrieve a specific category by its ID (Public)"
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryDto>> getCategoryById(
            @Parameter(description = "Category ID") @PathVariable UUID id) {
        CategoryDto category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(ApiResponse.success("Category retrieved successfully", category));
    }

    @Operation(
            summary = "Get category by slug",
            description = "Retrieve a category by its SEO-friendly slug (Public)"
    )
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<CategoryDto>> getCategoryBySlug(
            @Parameter(description = "Category slug") @PathVariable String slug) {
        CategoryDto category = categoryService.getCategoryBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success("Category retrieved successfully", category));
    }

    @Operation(
            summary = "Get sub-categories",
            description = "Retrieve all sub-categories of a parent category (Public)"
    )
    @GetMapping("/{id}/children")
    public ResponseEntity<ApiResponse<List<CategoryDto>>> getSubCategories(
            @Parameter(description = "Parent category ID") @PathVariable UUID id) {
        List<CategoryDto> categories = categoryService.getCategoriesByParentId(id);
        return ResponseEntity.ok(ApiResponse.success("Sub-categories retrieved successfully", categories));
    }

    @Operation(
            summary = "Create category",
            description = "Create a new category (Admin only)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Category created successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Category with the same name already exists"
            )
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<CategoryDto>> createCategory(
            @Valid @RequestBody CategoryRequest request) {
        CategoryDto created = categoryService.createCategory(request);
        return ResponseEntity.status(201)
                .body(ApiResponse.created("Category created successfully", created));
    }

    @Operation(
            summary = "Update category",
            description = "Update an existing category (Admin only)"
    )
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<CategoryDto>> updateCategory(
            @PathVariable UUID id,
            @Valid @RequestBody CategoryRequest request) {
        CategoryDto updated = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(ApiResponse.success("Category updated successfully", updated));
    }

    @Operation(
            summary = "Delete category",
            description = "Soft delete a category (Admin only)"
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable UUID id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success("Category deleted successfully", null));
    }

    @Operation(
            summary = "Restore category",
            description = "Restore a soft-deleted category (Admin only)"
    )
    @PatchMapping("/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<CategoryDto>> restoreCategory(@PathVariable UUID id) {
        CategoryDto restored = categoryService.restoreCategory(id);
        return ResponseEntity.ok(ApiResponse.success("Category restored successfully", restored));
    }
}