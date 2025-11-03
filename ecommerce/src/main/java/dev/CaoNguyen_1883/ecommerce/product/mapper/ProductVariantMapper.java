package dev.CaoNguyen_1883.ecommerce.product.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.CaoNguyen_1883.ecommerce.product.dto.ProductVariantDto;
import dev.CaoNguyen_1883.ecommerce.product.dto.ProductVariantRequest;
import dev.CaoNguyen_1883.ecommerce.product.entity.ProductVariant;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Mapper(componentModel = "spring", uses = {ProductImageMapper.class})
public abstract class ProductVariantMapper {

    @Autowired
    protected ObjectMapper objectMapper;

    @Mapping(target = "availableStock", expression = "java(variant.getAvailableStock())")
    @Mapping(target = "specifications", expression = "java(jsonToMap(variant.getSpecifications()))")
    @Mapping(target = "attributes", expression = "java(jsonToMap(variant.getAttributes()))")
    public abstract ProductVariantDto toDto(ProductVariant variant);

    public abstract List<ProductVariantDto> toDtoList(List<ProductVariant> variants);

    public abstract Set<ProductVariantDto> toDtoSet(Set<ProductVariant> variants);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "reservedStock", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "specifications", expression = "java(jsonNodeToString(request.getSpecifications()))")
    @Mapping(target = "attributes", expression = "java(jsonNodeToString(request.getAttributes()))")
    public abstract ProductVariant toEntity(ProductVariantRequest request);

    // Helper methods for JSON conversion
    protected Map<String, Object> jsonToMap(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    protected String jsonNodeToString(JsonNode node) {
        if (node == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
