package com.dom.inventory.service.impl;

import com.dom.common.exception.InsufficientStockException;
import com.dom.common.exception.ResourceNotFoundException;
import com.dom.dto.InventoryDTO;
import com.dom.inventory.model.Inventory;
import com.dom.inventory.model.StockReservation;
import com.dom.inventory.repository.InventoryRepository;
import com.dom.inventory.repository.StockReservationRepository;
import com.dom.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Inventory service implementation with optimistic locking for concurrent stock updates
 * and Redis caching for read operations.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class InventoryServiceImpl implements InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryServiceImpl.class);

    private final InventoryRepository inventoryRepository;
    private final StockReservationRepository reservationRepository;

    @Override
    @Cacheable(value = "inventory", key = "#productId")
    @Transactional(readOnly = true)
    public InventoryDTO getStock(Long productId) {
        log.info("Fetching inventory for product {} (cache miss)", productId);
        Inventory inventory = findByProductId(productId);
        return toDTO(inventory);
    }

    @Override
    @CacheEvict(value = "inventory", key = "#productId")
    public InventoryDTO updateStock(Long productId, InventoryDTO dto) {
        log.info("Updating stock for product {}: quantity={}", productId, dto.getAvailableQuantity());
        Inventory inventory = findByProductId(productId);
        inventory.setAvailableQuantity(dto.getAvailableQuantity());
        inventory = inventoryRepository.save(inventory);
        log.info("Stock updated for product {}. New quantity: {}", productId, inventory.getAvailableQuantity());
        return toDTO(inventory);
    }

    @Override
    @CacheEvict(value = "inventory", key = "#productId")
    public InventoryDTO reserveStock(Long productId, int quantity, Long orderId) {
        log.info("Reserving {} units for product {}, order {}", quantity, productId, orderId);
        Inventory inventory = findByProductId(productId);

        if (inventory.getAvailableQuantity() < quantity) {
            throw new InsufficientStockException(productId, quantity, inventory.getAvailableQuantity());
        }

        inventory.setAvailableQuantity(inventory.getAvailableQuantity() - quantity);
        inventory.setReservedQuantity(inventory.getReservedQuantity() + quantity);
        inventory = inventoryRepository.save(inventory);

        // Create reservation record
        StockReservation reservation = StockReservation.builder()
                .inventoryId(inventory.getId())
                .orderId(orderId)
                .quantity(quantity)
                .status("RESERVED")
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .build();
        reservationRepository.save(reservation);

        log.info("Stock reserved for product {}. Available: {}, Reserved: {}",
                productId, inventory.getAvailableQuantity(), inventory.getReservedQuantity());
        return toDTO(inventory);
    }

    @Override
    @CacheEvict(value = "inventory", key = "#productId")
    public InventoryDTO releaseStock(Long productId, int quantity, Long orderId) {
        log.info("Releasing {} units for product {}, order {}", quantity, productId, orderId);
        Inventory inventory = findByProductId(productId);

        int releaseQty = Math.min(quantity, inventory.getReservedQuantity());
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() + releaseQty);
        inventory.setReservedQuantity(inventory.getReservedQuantity() - releaseQty);
        inventory = inventoryRepository.save(inventory);

        // Update reservation status
        reservationRepository.findByOrderId(orderId).forEach(r -> {
            r.setStatus("RELEASED");
            reservationRepository.save(r);
        });

        log.info("Stock released for product {}. Available: {}, Reserved: {}",
                productId, inventory.getAvailableQuantity(), inventory.getReservedQuantity());
        return toDTO(inventory);
    }

    private Inventory findByProductId(Long productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "productId=" + productId));
    }

    private InventoryDTO toDTO(Inventory inventory) {
        return InventoryDTO.builder()
                .id(inventory.getId())
                .productId(inventory.getProductId())
                .availableQuantity(inventory.getAvailableQuantity())
                .reservedQuantity(inventory.getReservedQuantity())
                .build();
    }
}
