package dev.CaoNguyen_1883.ecommerce.product.service.impl;

import dev.CaoNguyen_1883.ecommerce.common.exception.BadRequestException;
import dev.CaoNguyen_1883.ecommerce.common.exception.ForbiddenException;
import dev.CaoNguyen_1883.ecommerce.common.exception.ResourceNotFoundException;
import dev.CaoNguyen_1883.ecommerce.product.dto.ProductImageDto;
import dev.CaoNguyen_1883.ecommerce.product.dto.ProductImageRequest;
import dev.CaoNguyen_1883.ecommerce.product.entity.Product;
import dev.CaoNguyen_1883.ecommerce.product.entity.ProductImage;
import dev.CaoNguyen_1883.ecommerce.product.entity.ProductVariant;
import dev.CaoNguyen_1883.ecommerce.product.mapper.ProductImageMapper;
import dev.CaoNguyen_1883.ecommerce.product.repository.ProductImageRepository;
import dev.CaoNguyen_1883.ecommerce.product.repository.ProductRepository;
import dev.CaoNguyen_1883.ecommerce.product.repository.ProductVariantRepository;
import dev.CaoNguyen_1883.ecommerce.product.service.IProductImageService;
import dev.CaoNguyen_1883.ecommerce.storage.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductImageServiceImpl implements IProductImageService {

    private final ProductImageRepository imageRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductImageMapper imageMapper;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional
    public ProductImageDto addProductImage(UUID productId, ProductImageRequest request, UUID userId) {
        log.info("Adding image to product: {}", productId);

        // Validate product exists and user owns it
        Product product = validateProductOwnership(productId, userId);

        // Create image entity
        ProductImage image = ProductImage.builder()
                .product(product)
                .variant(null) // Product-level image, not variant-specific
                .imageUrl(request.getImageUrl())
                .altText(request.getAltText())
                .isPrimary(request.getIsPrimary() != null ? request.getIsPrimary() : false)
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .build();

        // If this is set as primary, unset other primary images
        if (image.getIsPrimary()) {
            imageRepository.findPrimaryImageByProductId(productId)
                    .ifPresent(existingPrimary -> {
                        existingPrimary.setIsPrimary(false);
                        imageRepository.save(existingPrimary);
                    });
        }

        ProductImage saved = imageRepository.save(image);
        log.info("Image added successfully to product: {}", productId);

        return imageMapper.toDto(saved);
    }

    @Override
    @Transactional
    public ProductImageDto addVariantImage(UUID productId, UUID variantId, ProductImageRequest request, UUID userId) {
        log.info("Adding image to variant: {} of product: {}", variantId, productId);

        // Validate product exists and user owns it
        Product product = validateProductOwnership(productId, userId);

        // Validate variant exists and belongs to this product
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", "id", variantId));

        if (!variant.getProduct().getId().equals(productId)) {
            throw new BadRequestException("Variant does not belong to this product");
        }

        // Create image entity
        ProductImage image = ProductImage.builder()
                .product(product)
                .variant(variant)
                .imageUrl(request.getImageUrl())
                .altText(request.getAltText())
                .isPrimary(request.getIsPrimary() != null ? request.getIsPrimary() : false)
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .build();

        // If this is set as primary, unset other primary images for this variant
        if (image.getIsPrimary()) {
            imageRepository.findPrimaryImageByVariantId(variantId)
                    .ifPresent(existingPrimary -> {
                        existingPrimary.setIsPrimary(false);
                        imageRepository.save(existingPrimary);
                    });
        }

        ProductImage saved = imageRepository.save(image);
        log.info("Image added successfully to variant: {}", variantId);

        return imageMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteProductImage(UUID productId, UUID imageId, UUID userId) {
        log.info("Deleting image: {} from product: {}", imageId, productId);

        // Validate product ownership
        validateProductOwnership(productId, userId);

        // Find the image
        ProductImage image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductImage", "id", imageId));

        // Verify image belongs to this product
        if (!image.getProduct().getId().equals(productId)) {
            throw new BadRequestException("Image does not belong to this product");
        }

        // Extract object name from URL and delete from MinIO
        try {
            String objectName = fileStorageService.extractObjectNameFromUrl(image.getImageUrl());
            fileStorageService.deleteFile(objectName);
            log.debug("Deleted file from MinIO: {}", objectName);
        } catch (Exception e) {
            log.warn("Failed to delete file from MinIO, continuing with database deletion: {}", e.getMessage());
        }

        // Delete from database
        imageRepository.delete(image);
        log.info("Image deleted successfully: {}", imageId);
    }

    @Override
    @Transactional
    public void setPrimaryImage(UUID productId, UUID imageId, UUID userId) {
        log.info("Setting primary image: {} for product: {}", imageId, productId);

        // Validate product ownership
        validateProductOwnership(productId, userId);

        // Find the image
        ProductImage image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductImage", "id", imageId));

        // Verify image belongs to this product
        if (!image.getProduct().getId().equals(productId)) {
            throw new BadRequestException("Image does not belong to this product");
        }

        // Unset current primary image
        if (image.getVariant() != null) {
            // Variant-specific image
            imageRepository.findPrimaryImageByVariantId(image.getVariant().getId())
                    .ifPresent(existingPrimary -> {
                        if (!existingPrimary.getId().equals(imageId)) {
                            existingPrimary.setIsPrimary(false);
                            imageRepository.save(existingPrimary);
                        }
                    });
        } else {
            // Product-level image
            imageRepository.findPrimaryImageByProductId(productId)
                    .ifPresent(existingPrimary -> {
                        if (!existingPrimary.getId().equals(imageId)) {
                            existingPrimary.setIsPrimary(false);
                            imageRepository.save(existingPrimary);
                        }
                    });
        }

        // Set this as primary
        image.setIsPrimary(true);
        imageRepository.save(image);

        log.info("Primary image set successfully: {}", imageId);
    }

    @Override
    @Transactional
    public void reorderImages(UUID productId, List<UUID> imageIds, UUID userId) {
        log.info("Reordering {} images for product: {}", imageIds.size(), productId);

        // Validate product ownership
        validateProductOwnership(productId, userId);

        // Update display order for each image
        for (int i = 0; i < imageIds.size(); i++) {
            UUID imageId = imageIds.get(i);
            ProductImage image = imageRepository.findById(imageId)
                    .orElseThrow(() -> new ResourceNotFoundException("ProductImage", "id", imageId));

            // Verify image belongs to this product
            if (!image.getProduct().getId().equals(productId)) {
                throw new BadRequestException("Image " + imageId + " does not belong to this product");
            }

            image.setDisplayOrder(i);
            imageRepository.save(image);
        }

        log.info("Images reordered successfully for product: {}", productId);
    }

    @Override
    public List<ProductImageDto> getProductImages(UUID productId) {
        log.debug("Fetching images for product: {}", productId);

        // Validate product exists
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product", "id", productId);
        }

        List<ProductImage> images = imageRepository.findByProductId(productId);
        return imageMapper.toDtoList(images);
    }

    @Override
    public List<ProductImageDto> getVariantImages(UUID variantId) {
        log.debug("Fetching images for variant: {}", variantId);

        // Validate variant exists
        if (!variantRepository.existsById(variantId)) {
            throw new ResourceNotFoundException("ProductVariant", "id", variantId);
        }

        List<ProductImage> images = imageRepository.findByVariantId(variantId);
        return imageMapper.toDtoList(images);
    }

    /**
     * Helper method to validate product exists and user owns it (or is admin)
     */
    private Product validateProductOwnership(UUID productId, UUID userId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        // Check ownership (seller can only manage their own products)
        // Note: Admin/Staff check should be done at controller level with @PreAuthorize
        if (!product.getSeller().getId().equals(userId)) {
            throw new ForbiddenException("You can only manage images for your own products");
        }

        return product;
    }
}
