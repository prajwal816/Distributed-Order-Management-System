package com.dom.inventory.service;

import com.dom.dto.InventoryDTO;

/**
 * Inventory service interface for stock management operations.
 */
public interface InventoryService {

    InventoryDTO getStock(Long productId);

    InventoryDTO updateStock(Long productId, InventoryDTO inventoryDTO);

    InventoryDTO reserveStock(Long productId, int quantity, Long orderId);

    InventoryDTO releaseStock(Long productId, int quantity, Long orderId);
}
