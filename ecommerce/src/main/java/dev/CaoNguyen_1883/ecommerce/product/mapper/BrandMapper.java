package dev.CaoNguyen_1883.ecommerce.product.mapper;

import dev.CaoNguyen_1883.ecommerce.product.dto.BrandDto;
import dev.CaoNguyen_1883.ecommerce.product.dto.BrandRequest;
import dev.CaoNguyen_1883.ecommerce.product.entity.Brand;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BrandMapper {

    BrandDto toDto(Brand brand);

    List<BrandDto> toDtoList(List<Brand> brands);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "slug", ignore = true)
    Brand toEntity(BrandRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "slug", ignore = true)
    void updateEntityFromRequest(BrandRequest request, @MappingTarget Brand brand);
}

