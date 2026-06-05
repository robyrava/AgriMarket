package com.agrimarket.catalog.mapper;

import com.agrimarket.catalog.domain.Inventory;
import com.agrimarket.catalog.dto.InventoryDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface InventoryMapper {
    InventoryMapper INSTANCE = Mappers.getMapper(InventoryMapper.class);

    InventoryDTO toDTO(Inventory inventory);
    Inventory toEntity(InventoryDTO inventoryDTO);
    void updateEntityFromDto(InventoryDTO inventoryDTO, @MappingTarget Inventory inventory);
}
