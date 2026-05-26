package com.dom.cart.service;

import com.dom.dto.CartDTO;
import com.dom.dto.CartItemDTO;

/**
 * Cart service interface for shopping cart operations.
 */
public interface CartService {

    CartDTO getCart(Long userId);

    CartDTO addItem(Long userId, CartItemDTO itemDTO);

    CartDTO updateItemQuantity(Long userId, Long itemId, Integer quantity);

    CartDTO removeItem(Long userId, Long itemId);

    void clearCart(Long userId);
}
