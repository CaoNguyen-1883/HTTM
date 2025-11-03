package dev.CaoNguyen_1883.ecommerce.product.controller;

import dev.CaoNguyen_1883.ecommerce.common.response.ApiResponse;
import dev.CaoNguyen_1883.ecommerce.product.dto.BrandDto;
import dev.CaoNguyen_1883.ecommerce.product.dto.BrandRequest;
import dev.CaoNguyen_1883.ecommerce.product.service.IBrandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/brands")
@RequiredArgsConstructor
@Tag(name = "Brand Management", description = "APIs for managing product brands")
public class BrandController {

    private final IBrandService brandService;

    @Operation(
            summary = "Get all brands",
            description = "Retrieve all active brands (Public)"
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<BrandDto>>> getAllBrands() {
        List<BrandDto> brands = brandService.getAllBrands();
        return ResponseEntity.ok(ApiResponse.success("Brands retrieved successfully", brands));
    }

    @Operation(
            summary = "Get featured brands",
            description = "Retrieve featured brands for homepage (Public)"
    )
    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<List<BrandDto>>> getFeaturedBrands() {
        List<BrandDto> brands = brandService.getFeaturedBrands();
        return ResponseEntity.ok(ApiResponse.success("Featured brands retrieved successfully", brands));
    }

    @Operation(
            summary = "Get brand by ID",
            description = "Retrieve a specific brand by its ID (Public)"
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BrandDto>> getBrandById(
            @Parameter(description = "Brand ID") @PathVariable UUID id) {
        BrandDto brand = brandService.getBrandById(id);
        return ResponseEntity.ok(ApiResponse.success("Brand retrieved successfully", brand));
    }

    @Operation(
            summary = "Get brand by slug",
            description = "Retrieve a brand by its SEO-friendly slug (Public)"
    )
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<BrandDto>> getBrandBySlug(
            @Parameter(description = "Brand slug") @PathVariable String slug) {
        BrandDto brand = brandService.getBrandBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success("Brand retrieved successfully", brand));
    }

    @Operation(
            summary = "Create brand",
            description = "Create a new brand (Admin only)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Brand created successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Brand with the same name already exists"
            )
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<BrandDto>> createBrand(
            @Valid @RequestBody BrandRequest request) {
        BrandDto created = brandService.createBrand(request);
        return ResponseEntity.status(201)
                .body(ApiResponse.created("Brand created successfully", created));
    }

    @Operation(
            summary = "Update brand",
            description = "Update an existing brand (Admin only)"
    )
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<BrandDto>> updateBrand(
            @PathVariable UUID id,
            @Valid @RequestBody BrandRequest request) {
        BrandDto updated = brandService.updateBrand(id, request);
        return ResponseEntity.ok(ApiResponse.success("Brand updated successfully", updated));
    }

    @Operation(
            summary = "Delete brand",
            description = "Soft delete a brand (Admin only)"
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<Void>> deleteBrand(@PathVariable UUID id) {
        brandService.deleteBrand(id);
        return ResponseEntity.ok(ApiResponse.success("Brand deleted successfully", null));
    }

    @Operation(
            summary = "Restore brand",
            description = "Restore a soft-deleted brand (Admin only)"
    )
    @PatchMapping("/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<BrandDto>> restoreBrand(@PathVariable UUID id) {
        BrandDto restored = brandService.restoreBrand(id);
        return ResponseEntity.ok(ApiResponse.success("Brand restored successfully", restored));
    }
}
