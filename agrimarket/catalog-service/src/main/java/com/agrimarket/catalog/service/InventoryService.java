package com.agrimarket.catalog.service;

import com.agrimarket.catalog.domain.Inventory;
import com.agrimarket.catalog.dto.InventoryDTO;
import com.agrimarket.catalog.exception.InventoryNotFoundException;
import com.agrimarket.catalog.mapper.InventoryMapper;
import com.agrimarket.catalog.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryMapper inventoryMapper;

    @Transactional(readOnly = true)
    public InventoryDTO getInventoryByProductId(Long productId) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new InventoryNotFoundException(productId));
        return inventoryMapper.toDTO(inventory);
    }

    @Transactional
    public InventoryDTO saveOrUpdateInventory(InventoryDTO inventoryDTO) {
        Inventory inventory = inventoryRepository.findByProductId(inventoryDTO.getProductId())
                .orElse(new Inventory());
        
        inventoryMapper.updateEntityFromDto(inventoryDTO, inventory);
        // Ensure productId is set in case it's a new inventory
        if (inventory.getProductId() == null) {
            inventory.setProductId(inventoryDTO.getProductId());
        }
        
        Inventory savedInventory = inventoryRepository.save(inventory);
        return inventoryMapper.toDTO(savedInventory);
    }
}
