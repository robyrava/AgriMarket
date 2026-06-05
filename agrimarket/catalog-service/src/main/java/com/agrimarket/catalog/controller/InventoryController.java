package com.agrimarket.catalog.controller;

import com.agrimarket.catalog.dto.InventoryDTO;
import com.agrimarket.catalog.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/catalog/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/product/{productId}")
    public ResponseEntity<InventoryDTO> getInventoryByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(inventoryService.getInventoryByProductId(productId));
    }

    @PostMapping
    public ResponseEntity<InventoryDTO> saveOrUpdateInventory(@RequestBody InventoryDTO inventoryDTO) {
        return ResponseEntity.ok(inventoryService.saveOrUpdateInventory(inventoryDTO));
    }
}
