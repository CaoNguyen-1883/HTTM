package dev.CaoNguyen_1883.ecommerce.product.controller;

import dev.CaoNguyen_1883.ecommerce.auth.security.CustomUserDetails;
import dev.CaoNguyen_1883.ecommerce.common.response.ApiResponse;
import dev.CaoNguyen_1883.ecommerce.product.dto.ProductImageDto;
import dev.CaoNguyen_1883.ecommerce.product.dto.ProductImageRequest;
import dev.CaoNguyen_1883.ecommerce.product.service.IProductImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/products/{productId}/images")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Product Image Management", description = "APIs for managing product and variant images")
public class ProductImageController {

    private final IProductImageService imageService;

    // ===== PRODUCT IMAGE ENDPOINTS =====

    @Operation(
            summary = "Add image to product",
            description = "Add a new image to a product (upload the file first via /api/files/upload/product)"
    )
    @PostMapping
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<ProductImageDto>> addProductImage(
            @Parameter(description = "Product ID") @PathVariable UUID productId,
            @Valid @RequestBody ProductImageRequest request,
            Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        ProductImageDto image = imageService.addProductImage(productId, request, userDetails.getId());

        return ResponseEntity.status(201)
                .body(ApiResponse.created("Image added to product successfully", image));
    }

    @Operation(
            summary = "Get product images",
            description = "Get all images for a product (Public)"
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductImageDto>>> getProductImages(
            @Parameter(description = "Product ID") @PathVariable UUID productId) {

        List<ProductImageDto> images = imageService.getProductImages(productId);
        return ResponseEntity.ok(ApiResponse.success("Product images retrieved successfully", images));
    }

    @Operation(
            summary = "Delete product image",
            description = "Delete an image from a product (also deletes from MinIO)"
    )
    @DeleteMapping("/{imageId}")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<Void>> deleteProductImage(
            @Parameter(description = "Product ID") @PathVariable UUID productId,
            @Parameter(description = "Image ID") @PathVariable UUID imageId,
            Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        imageService.deleteProductImage(productId, imageId, userDetails.getId());

        return ResponseEntity.ok(ApiResponse.success("Image deleted successfully", null));
    }

    @Operation(
            summary = "Set primary image",
            description = "Set an image as the primary/main image for a product or variant"
    )
    @PatchMapping("/{imageId}/set-primary")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<Void>> setPrimaryImage(
            @Parameter(description = "Product ID") @PathVariable UUID productId,
            @Parameter(description = "Image ID") @PathVariable UUID imageId,
            Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        imageService.setPrimaryImage(productId, imageId, userDetails.getId());

        return ResponseEntity.ok(ApiResponse.success("Primary image set successfully", null));
    }

    @Operation(
            summary = "Reorder images",
            description = "Reorder images for a product by providing list of image IDs in desired order"
    )
    @PatchMapping("/reorder")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<Void>> reorderImages(
            @Parameter(description = "Product ID") @PathVariable UUID productId,
            @Parameter(description = "List of image IDs in desired order")
            @RequestBody List<UUID> imageIds,
            Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        imageService.reorderImages(productId, imageIds, userDetails.getId());

        return ResponseEntity.ok(ApiResponse.success("Images reordered successfully", null));
    }

    // ===== VARIANT IMAGE ENDPOINTS =====

    @Operation(
            summary = "Add image to variant",
            description = "Add a new image to a specific product variant"
    )
    @PostMapping("/variants/{variantId}")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<ProductImageDto>> addVariantImage(
            @Parameter(description = "Product ID") @PathVariable UUID productId,
            @Parameter(description = "Variant ID") @PathVariable UUID variantId,
            @Valid @RequestBody ProductImageRequest request,
            Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        ProductImageDto image = imageService.addVariantImage(productId, variantId, request, userDetails.getId());

        return ResponseEntity.status(201)
                .body(ApiResponse.created("Image added to variant successfully", image));
    }

    @Operation(
            summary = "Get variant images",
            description = "Get all images for a specific variant (Public)"
    )
    @GetMapping("/variants/{variantId}")
    public ResponseEntity<ApiResponse<List<ProductImageDto>>> getVariantImages(
            @Parameter(description = "Variant ID") @PathVariable UUID variantId) {

        List<ProductImageDto> images = imageService.getVariantImages(variantId);
        return ResponseEntity.ok(ApiResponse.success("Variant images retrieved successfully", images));
    }
}
