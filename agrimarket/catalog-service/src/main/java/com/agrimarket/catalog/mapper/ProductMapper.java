package com.agrimarket.catalog.mapper;

import com.agrimarket.catalog.domain.Product;
import com.agrimarket.catalog.dto.ProductDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

    ProductDTO toDTO(Product product);
    Product toEntity(ProductDTO productDTO);
    void updateEntityFromDto(ProductDTO productDTO, @MappingTarget Product product);
}
