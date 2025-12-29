package dev.CaoNguyen_1883.ecommerce.product.service;

import dev.CaoNguyen_1883.ecommerce.product.dto.ProductImageDto;
import dev.CaoNguyen_1883.ecommerce.product.dto.ProductImageRequest;

import java.util.List;
import java.util.UUID;

public interface IProductImageService {

    /**
     * Add a new image to a product
     * @param productId The product ID
     * @param request The image request with URL and metadata
     * @param userId The user ID (seller/admin)
     * @return The created image DTO
     */
    ProductImageDto addProductImage(UUID productId, ProductImageRequest request, UUID userId);

    /**
     * Add a new image to a product variant
     * @param productId The product ID
     * @param variantId The variant ID
     * @param request The image request with URL and metadata
     * @param userId The user ID (seller/admin)
     * @return The created image DTO
     */
    ProductImageDto addVariantImage(UUID productId, UUID variantId, ProductImageRequest request, UUID userId);

    /**
     * Delete an image from a product
     * @param productId The product ID
     * @param imageId The image ID
     * @param userId The user ID (seller/admin) - for ownership validation
     */
    void deleteProductImage(UUID productId, UUID imageId, UUID userId);

    /**
     * Set an image as primary for a product
     * @param productId The product ID
     * @param imageId The image ID to set as primary
     * @param userId The user ID (seller/admin)
     */
    void setPrimaryImage(UUID productId, UUID imageId, UUID userId);

    /**
     * Reorder images for a product
     * @param productId The product ID
     * @param imageIds The list of image IDs in the desired order
     * @param userId The user ID (seller/admin)
     */
    void reorderImages(UUID productId, List<UUID> imageIds, UUID userId);

    /**
     * Get all images for a product
     * @param productId The product ID
     * @return List of image DTOs
     */
    List<ProductImageDto> getProductImages(UUID productId);

    /**
     * Get all images for a product variant
     * @param variantId The variant ID
     * @return List of image DTOs
     */
    List<ProductImageDto> getVariantImages(UUID variantId);
}
