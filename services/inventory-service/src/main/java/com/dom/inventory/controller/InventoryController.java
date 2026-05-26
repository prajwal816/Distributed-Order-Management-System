package com.dom.inventory.controller;

import com.dom.common.response.ApiResponse;
import com.dom.dto.InventoryDTO;
import com.dom.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Inventory Service endpoints.
 * Provides stock management, reservation, and release capabilities.
 */
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private static final Logger log = LoggerFactory.getLogger(InventoryController.class);
    private final InventoryService inventoryService;

    /**
     * GET /api/inventory/{productId} - Get stock level for a product.
     */
    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<InventoryDTO>> getStock(@PathVariable Long productId) {
        log.info("GET /api/inventory/{}", productId);
        InventoryDTO inventory = inventoryService.getStock(productId);
        return ResponseEntity.ok(ApiResponse.ok(inventory));
    }

    /**
     * PUT /api/inventory/{productId} - Update stock quantity.
     */
    @PutMapping("/{productId}")
    public ResponseEntity<ApiResponse<InventoryDTO>> updateStock(
            @PathVariable Long productId,
            @Valid @RequestBody InventoryDTO inventoryDTO) {
        log.info("PUT /api/inventory/{}", productId);
        InventoryDTO updated = inventoryService.updateStock(productId, inventoryDTO);
        return ResponseEntity.ok(ApiResponse.ok(updated, "Stock updated successfully"));
    }

    /**
     * POST /api/inventory/{productId}/reserve - Reserve stock for an order.
     */
    @PostMapping("/{productId}/reserve")
    public ResponseEntity<ApiResponse<InventoryDTO>> reserveStock(
            @PathVariable Long productId,
            @RequestParam int quantity,
            @RequestParam(required = false, defaultValue = "0") Long orderId) {
        log.info("POST /api/inventory/{}/reserve - qty={}, orderId={}", productId, quantity, orderId);
        InventoryDTO result = inventoryService.reserveStock(productId, quantity, orderId);
        return ResponseEntity.ok(ApiResponse.ok(result, "Stock reserved successfully"));
    }

    /**
     * POST /api/inventory/{productId}/release - Release reserved stock.
     */
    @PostMapping("/{productId}/release")
    public ResponseEntity<ApiResponse<InventoryDTO>> releaseStock(
            @PathVariable Long productId,
            @RequestParam int quantity,
            @RequestParam(required = false, defaultValue = "0") Long orderId) {
        log.info("POST /api/inventory/{}/release - qty={}, orderId={}", productId, quantity, orderId);
        InventoryDTO result = inventoryService.releaseStock(productId, quantity, orderId);
        return ResponseEntity.ok(ApiResponse.ok(result, "Stock released successfully"));
    }
}
