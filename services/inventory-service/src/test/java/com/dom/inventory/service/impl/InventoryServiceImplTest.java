package com.dom.inventory.service.impl;

import com.dom.common.exception.InsufficientStockException;
import com.dom.common.exception.ResourceNotFoundException;
import com.dom.dto.InventoryDTO;
import com.dom.inventory.model.Inventory;
import com.dom.inventory.repository.InventoryRepository;
import com.dom.inventory.repository.StockReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryServiceImpl Unit Tests")
class InventoryServiceImplTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private StockReservationRepository reservationRepository;

    private InventoryServiceImpl inventoryService;

    private Inventory sampleInventory;

    @BeforeEach
    void setUp() {
        inventoryService = new InventoryServiceImpl(inventoryRepository, reservationRepository);
        sampleInventory = Inventory.builder()
                .id(1L)
                .productId(1L)
                .availableQuantity(50)
                .reservedQuantity(0)
                .version(0L)
                .build();
    }

    @Test
    @DisplayName("getStock returns inventory for existing product")
    void getStock_Found() {
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(sampleInventory));

        InventoryDTO result = inventoryService.getStock(1L);

        assertThat(result.getProductId()).isEqualTo(1L);
        assertThat(result.getAvailableQuantity()).isEqualTo(50);
    }

    @Test
    @DisplayName("getStock throws ResourceNotFoundException for unknown product")
    void getStock_NotFound() {
        when(inventoryRepository.findByProductId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.getStock(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("reserveStock decreases available and increases reserved")
    void reserveStock_Success() {
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(sampleInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(sampleInventory);

        InventoryDTO result = inventoryService.reserveStock(1L, 10, 100L);

        assertThat(sampleInventory.getAvailableQuantity()).isEqualTo(40);
        assertThat(sampleInventory.getReservedQuantity()).isEqualTo(10);
        verify(reservationRepository).save(any());
    }

    @Test
    @DisplayName("reserveStock throws InsufficientStockException when not enough stock")
    void reserveStock_InsufficientStock() {
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(sampleInventory));

        assertThatThrownBy(() -> inventoryService.reserveStock(1L, 100, 100L))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Insufficient stock");
    }

    @Test
    @DisplayName("updateStock sets new available quantity")
    void updateStock_Success() {
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(sampleInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(sampleInventory);

        InventoryDTO dto = InventoryDTO.builder().availableQuantity(200).build();
        inventoryService.updateStock(1L, dto);

        assertThat(sampleInventory.getAvailableQuantity()).isEqualTo(200);
    }

    @Test
    @DisplayName("releaseStock increases available and decreases reserved")
    void releaseStock_Success() {
        sampleInventory.setAvailableQuantity(40);
        sampleInventory.setReservedQuantity(10);
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(sampleInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(sampleInventory);
        when(reservationRepository.findByOrderId(100L)).thenReturn(java.util.List.of());

        inventoryService.releaseStock(1L, 10, 100L);

        assertThat(sampleInventory.getAvailableQuantity()).isEqualTo(50);
        assertThat(sampleInventory.getReservedQuantity()).isEqualTo(0);
    }
}
