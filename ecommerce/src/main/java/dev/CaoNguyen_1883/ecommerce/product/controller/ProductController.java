package dev.CaoNguyen_1883.ecommerce.product.controller;

import dev.CaoNguyen_1883.ecommerce.auth.security.CustomUserDetails;
import dev.CaoNguyen_1883.ecommerce.common.response.ApiResponse;
import dev.CaoNguyen_1883.ecommerce.product.dto.*;
import dev.CaoNguyen_1883.ecommerce.product.entity.ProductStatus;
import dev.CaoNguyen_1883.ecommerce.product.service.IProductService;
import dev.CaoNguyen_1883.ecommerce.tracking.service.ProductViewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(
    name = "Product Management",
    description = "APIs for managing products with variants and approval workflow"
)
public class ProductController {

    private final IProductService productService;
    private final ProductViewService viewService;

    // ===== PUBLIC ENDPOINTS =====

    @Operation(
        summary = "Get all products",
        description = "Get all approved products with optional filters (keyword, category, brand, price range) and pagination (Public)"
    )
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductSummaryDto>>> getAllProducts(
        @Parameter(
            description = "Search keyword",
            example = "laptop"
        ) @RequestParam(required = false) String keyword,
        @Parameter(description = "Category ID") @RequestParam(
            required = false
        ) UUID categoryId,
        @Parameter(description = "Brand ID") @RequestParam(
            required = false
        ) UUID brandId,
        @Parameter(description = "Minimum price") @RequestParam(
            required = false
        ) Double minPrice,
        @Parameter(description = "Maximum price") @RequestParam(
            required = false
        ) Double maxPrice,
        @PageableDefault(
            size = 20,
            sort = "createdAt",
            direction = Sort.Direction.DESC
        ) Pageable pageable
    ) {
        Page<ProductSummaryDto> products;

        // If any filter is provided, use filterProducts method
        if (
            keyword != null ||
            categoryId != null ||
            brandId != null ||
            minPrice != null ||
            maxPrice != null
        ) {
            products = productService.filterProducts(
                keyword,
                categoryId,
                brandId,
                minPrice,
                maxPrice,
                pageable
            );
        } else {
            products = productService.getAllProducts(pageable);
        }

        return ResponseEntity.ok(
            ApiResponse.success("Products retrieved successfully", products)
        );
    }

    @Operation(
        summary = "Search products",
        description = "Search products by keyword in name and description (Public)"
    )
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<ProductSummaryDto>>> searchProducts(
        @Parameter(
            description = "Search keyword",
            example = "gaming mouse"
        ) @RequestParam String keyword,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<ProductSummaryDto> products = productService.searchProducts(
            keyword,
            pageable
        );
        return ResponseEntity.ok(
            ApiResponse.success("Search completed successfully", products)
        );
    }

    @Operation(
        summary = "Filter by category",
        description = "Get products by category ID (Public)"
    )
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<
        ApiResponse<Page<ProductSummaryDto>>
    > getProductsByCategory(
        @PathVariable UUID categoryId,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<ProductSummaryDto> products = productService.getProductsByCategory(
            categoryId,
            pageable
        );
        return ResponseEntity.ok(
            ApiResponse.success("Products retrieved successfully", products)
        );
    }

    @Operation(
        summary = "Filter by brand",
        description = "Get products by brand ID (Public)"
    )
    @GetMapping("/brand/{brandId}")
    public ResponseEntity<
        ApiResponse<Page<ProductSummaryDto>>
    > getProductsByBrand(
        @PathVariable UUID brandId,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<ProductSummaryDto> products = productService.getProductsByBrand(
            brandId,
            pageable
        );
        return ResponseEntity.ok(
            ApiResponse.success("Products retrieved successfully", products)
        );
    }

    @Operation(
        summary = "Get product details",
        description = "Get detailed product information by ID (Public)"
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDto>> getProductById(
        @PathVariable UUID id,
        Authentication authentication
    ) {
        // Get product details
        ProductDto product = productService.getProductById(id);

        // Increment view count (existing functionality)
        productService.incrementViewCount(id);

        // Track detailed view for recommendation system (ASYNC - non-blocking)
        UUID userId = getCurrentUserId(authentication);
        viewService.trackView(userId, id);

        return ResponseEntity.ok(
            ApiResponse.success("Product retrieved successfully", product)
        );
    }

    /**
     * Helper method to extract user ID from authentication
     * Returns null for guest users
     */
    private UUID getCurrentUserId(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof CustomUserDetails userDetails) {
                return userDetails.getId();
            }
        }
        return null; // Guest user
    }

    @Operation(
        summary = "Get product by slug",
        description = "Get product details by SEO-friendly slug (Public)"
    )
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<ProductDto>> getProductBySlug(
        @PathVariable String slug
    ) {
        ProductDto product = productService.getProductBySlug(slug);

        // Increment view count
        productService.incrementViewCount(product.getId());

        return ResponseEntity.ok(
            ApiResponse.success("Product retrieved successfully", product)
        );
    }

    @Operation(
        summary = "Get trending products",
        description = "Get products sorted by view count (Public)"
    )
    @GetMapping("/trending")
    public ResponseEntity<
        ApiResponse<Page<ProductSummaryDto>>
    > getTrendingProducts(@PageableDefault(size = 10) Pageable pageable) {
        Page<ProductSummaryDto> products = productService.getTrendingProducts(
            pageable
        );
        return ResponseEntity.ok(
            ApiResponse.success(
                "Trending products retrieved successfully",
                products
            )
        );
    }

    @Operation(
        summary = "Get best sellers",
        description = "Get products sorted by purchase count (Public)"
    )
    @GetMapping("/best-sellers")
    public ResponseEntity<
        ApiResponse<Page<ProductSummaryDto>>
    > getBestSellingProducts(@PageableDefault(size = 10) Pageable pageable) {
        Page<ProductSummaryDto> products =
            productService.getBestSellingProducts(pageable);
        return ResponseEntity.ok(
            ApiResponse.success(
                "Best selling products retrieved successfully",
                products
            )
        );
    }

    @Operation(
        summary = "Get top rated",
        description = "Get products sorted by rating (Public)"
    )
    @GetMapping("/top-rated")
    public ResponseEntity<
        ApiResponse<Page<ProductSummaryDto>>
    > getTopRatedProducts(@PageableDefault(size = 10) Pageable pageable) {
        Page<ProductSummaryDto> products = productService.getTopRatedProducts(
            pageable
        );
        return ResponseEntity.ok(
            ApiResponse.success(
                "Top rated products retrieved successfully",
                products
            )
        );
    }

    @Operation(
        summary = "Get similar products",
        description = "Get similar products based on category (Public)"
    )
    @GetMapping("/{id}/similar")
    public ResponseEntity<
        ApiResponse<List<ProductSummaryDto>>
    > getSimilarProducts(
        @PathVariable UUID id,
        @Parameter(
            description = "Number of similar products to return"
        ) @RequestParam(defaultValue = "5") int limit
    ) {
        List<ProductSummaryDto> products = productService.getSimilarProducts(
            id,
            limit
        );
        return ResponseEntity.ok(
            ApiResponse.success(
                "Similar products retrieved successfully",
                products
            )
        );
    }

    // ===== SELLER ENDPOINTS =====

    @Operation(
        summary = "Create product",
        description = "Create a new product with variants (Seller only)"
    )
    @ApiResponses(
        value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "201",
                description = "Product created successfully with PENDING status"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "Invalid input data"
            ),
        }
    )
    @PostMapping
    @PreAuthorize("hasRole('SELLER')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<ProductDto>> createProduct(
        @Valid @RequestBody ProductRequest request,
        Authentication authentication
    ) {
        CustomUserDetails userDetails =
            (CustomUserDetails) authentication.getPrincipal();
        ProductDto created = productService.createProduct(
            request,
            userDetails.getId()
        );
        return ResponseEntity.status(201).body(
            ApiResponse.created(
                "Product created successfully and pending approval",
                created
            )
        );
    }

    @Operation(
        summary = "Get my products",
        description = "Get all products created by the authenticated seller"
    )
    @GetMapping("/my-products")
    @PreAuthorize("hasRole('SELLER')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<Page<ProductSummaryDto>>> getMyProducts(
        Authentication authentication,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) ProductStatus status,
        @PageableDefault(
            size = 20,
            sort = "createdAt",
            direction = Sort.Direction.DESC
        ) Pageable pageable
    ) {
        CustomUserDetails userDetails =
            (CustomUserDetails) authentication.getPrincipal();
        Page<ProductSummaryDto> products = productService.getSellerProducts(
            userDetails.getId(),
            keyword,
            status,
            pageable
        );
        return ResponseEntity.ok(
            ApiResponse.success(
                "Your products retrieved successfully",
                products
            )
        );
    }

    @Operation(
        summary = "Update product",
        description = "Update own product (Seller only). Product will be set to PENDING after update."
    )
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SELLER')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<ProductDto>> updateProduct(
        @PathVariable UUID id,
        @Valid @RequestBody ProductUpdateRequest request,
        Authentication authentication
    ) {
        CustomUserDetails userDetails =
            (CustomUserDetails) authentication.getPrincipal();
        ProductDto updated = productService.updateProduct(
            id,
            request,
            userDetails.getId()
        );
        return ResponseEntity.ok(
            ApiResponse.success("Product updated successfully", updated)
        );
    }

    @Operation(
        summary = "Delete product",
        description = "Soft delete own product (Seller only)"
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SELLER')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
        @PathVariable UUID id,
        Authentication authentication
    ) {
        CustomUserDetails userDetails =
            (CustomUserDetails) authentication.getPrincipal();
        productService.deleteProduct(id, userDetails.getId());
        return ResponseEntity.ok(
            ApiResponse.success("Product deleted successfully", null)
        );
    }

    // ===== STAFF/ADMIN ENDPOINTS =====

    @Operation(
        summary = "Get pending products",
        description = "Get all products waiting for approval (Staff/Admin only)"
    )
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<
        ApiResponse<Page<ProductSummaryDto>>
    > getPendingProducts(
        @PageableDefault(
            size = 20,
            sort = "createdAt",
            direction = Sort.Direction.ASC
        ) Pageable pageable
    ) {
        Page<ProductSummaryDto> products = productService.getPendingProducts(
            pageable
        );
        return ResponseEntity.ok(
            ApiResponse.success(
                "Pending products retrieved successfully",
                products
            )
        );
    }

    @Operation(
        summary = "Get all products for admin",
        description = "Get all products regardless of status with optional keyword search (Staff/Admin only)"
    )
    @GetMapping("/admin/all")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<
        ApiResponse<Page<ProductSummaryDto>>
    > getAllProductsForAdmin(
        @Parameter(
            description = "Optional search keyword",
            example = "laptop"
        ) @RequestParam(required = false) String keyword,
        @PageableDefault(
            size = 20,
            sort = "createdAt",
            direction = Sort.Direction.DESC
        ) Pageable pageable
    ) {
        Page<ProductSummaryDto> products;
        if (keyword != null && !keyword.trim().isEmpty()) {
            products = productService.getAllProductsForAdminWithKeyword(
                keyword,
                pageable
            );
        } else {
            products = productService.getAllProductsForAdmin(pageable);
        }
        return ResponseEntity.ok(
            ApiResponse.success("All products retrieved successfully", products)
        );
    }

    @Operation(
        summary = "Get products by status",
        description = "Get products filtered by status with optional keyword search (Staff/Admin only)"
    )
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<
        ApiResponse<Page<ProductSummaryDto>>
    > getProductsByStatus(
        @Parameter(
            description = "Product status",
            example = "APPROVED"
        ) @PathVariable ProductStatus status,
        @Parameter(
            description = "Optional search keyword",
            example = "laptop"
        ) @RequestParam(required = false) String keyword,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<ProductSummaryDto> products;
        if (keyword != null && !keyword.trim().isEmpty()) {
            products = productService.getProductsByStatusAndKeyword(
                status,
                keyword,
                pageable
            );
        } else {
            products = productService.getProductsByStatus(status, pageable);
        }
        return ResponseEntity.ok(
            ApiResponse.success("Products retrieved successfully", products)
        );
    }

    @Operation(
        summary = "Approve product",
        description = "Approve a pending product (Staff/Admin only)"
    )
    @ApiResponses(
        value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "Product approved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "Product is not in PENDING status or missing variants"
            ),
        }
    )
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<ProductDto>> approveProduct(
        @PathVariable UUID id,
        Authentication authentication
    ) {
        CustomUserDetails userDetails =
            (CustomUserDetails) authentication.getPrincipal();
        ProductDto approved = productService.approveProduct(
            id,
            userDetails.getId()
        );
        return ResponseEntity.ok(
            ApiResponse.success("Product approved successfully", approved)
        );
    }

    @Operation(
        summary = "Reject product",
        description = "Reject a pending product with reason (Staff/Admin only)"
    )
    @ApiResponses(
        value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "Product rejected successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "Product is not in PENDING status or rejection reason missing"
            ),
        }
    )
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<ProductDto>> rejectProduct(
        @PathVariable UUID id,
        @RequestBody ProductApprovalRequest request,
        Authentication authentication
    ) {
        CustomUserDetails userDetails =
            (CustomUserDetails) authentication.getPrincipal();
        ProductDto rejected = productService.rejectProduct(
            id,
            request.getReason(),
            userDetails.getId()
        );
        return ResponseEntity.ok(
            ApiResponse.success("Product rejected successfully", rejected)
        );
    }
}
