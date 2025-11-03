package dev.CaoNguyen_1883.ecommerce.product.mapper;

import dev.CaoNguyen_1883.ecommerce.product.dto.ProductImageDto;
import dev.CaoNguyen_1883.ecommerce.product.entity.ProductImage;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface ProductImageMapper {

    ProductImageDto toDto(ProductImage image);

    List<ProductImageDto> toDtoList(List<ProductImage> images);

    Set<ProductImageDto> toDtoSet(Set<ProductImage> images);
}
