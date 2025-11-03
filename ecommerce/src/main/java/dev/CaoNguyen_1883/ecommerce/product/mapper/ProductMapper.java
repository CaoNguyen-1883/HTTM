package dev.CaoNguyen_1883.ecommerce.product.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.CaoNguyen_1883.ecommerce.product.dto.ProductDto;
import dev.CaoNguyen_1883.ecommerce.product.dto.ProductRequest;
import dev.CaoNguyen_1883.ecommerce.product.dto.ProductSummaryDto;
import dev.CaoNguyen_1883.ecommerce.product.entity.Product;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class, BrandMapper.class, ProductVariantMapper.class, ProductImageMapper.class})
public abstract class ProductMapper {

    @Autowired
    protected ObjectMapper objectMapper;

    @Mapping(target = "sellerId", source = "seller.id")
    @Mapping(target = "sellerName", source = "seller.fullName")
    @Mapping(target = "tags", expression = "java(jsonToList(product.getTags()))")
    public abstract ProductDto toDto(Product product);

    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "brandName", source = "brand.name")
    @Mapping(target = "minPrice", expression = "java(calculateMinPrice(product))")
    @Mapping(target = "maxPrice", expression = "java(calculateMaxPrice(product))")
    @Mapping(target = "primaryImage", expression = "java(getPrimaryImageUrl(product))")
    @Mapping(target = "hasStock", expression = "java(product.hasStock())")
    @Mapping(target = "defaultVariantId", expression = "java(getDefaultVariantId(product))")
    public abstract ProductSummaryDto toSummaryDto(Product product);

    public abstract List<ProductSummaryDto> toSummaryDtoList(List<Product> products);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "seller", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "approvedBy", ignore = true)
    @Mapping(target = "approvedAt", ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "purchaseCount", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "totalReviews", ignore = true)
    @Mapping(target = "variants", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "totalStock", ignore = true)
    @Mapping(target = "tags", expression = "java(listToJson(request.getTags()))")
    public abstract Product toEntity(ProductRequest request);

    // Helper methods
    protected List<String> jsonToList(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    protected String listToJson(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    protected BigDecimal calculateMinPrice(Product product) {
        if (product.getVariants() == null || product.getVariants().isEmpty()) {
            return product.getBasePrice();
        }
        return product.getVariants().stream()
                .map(v -> v.getPrice() != null ? v.getPrice() : product.getBasePrice())
                .min(BigDecimal::compareTo)
                .orElse(product.getBasePrice());
    }

    protected BigDecimal calculateMaxPrice(Product product) {
        if (product.getVariants() == null || product.getVariants().isEmpty()) {
            return product.getBasePrice();
        }
        return product.getVariants().stream()
                .map(v -> v.getPrice() != null ? v.getPrice() : product.getBasePrice())
                .max(BigDecimal::compareTo)
                .orElse(product.getBasePrice());
    }

    protected String getPrimaryImageUrl(Product product) {
        return product.getImages().stream()
                .filter(img -> img.getIsPrimary())
                .findFirst()
                .map(img -> img.getImageUrl())
                .orElse(null);
    }

    protected java.util.UUID getDefaultVariantId(Product product) {
        if (product.getVariants() == null || product.getVariants().isEmpty()) {
            return null;
        }
        // Return the first variant that has stock > 0
        return product.getVariants().stream()
                .filter(v -> v.getStock() != null && v.getStock() > 0)
                .findFirst()
                .map(v -> v.getId())
                .orElse(null);
    }
}