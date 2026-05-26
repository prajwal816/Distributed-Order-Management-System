package com.dom.cart.controller;

import com.dom.cart.service.CartService;
import com.dom.common.response.ApiResponse;
import com.dom.dto.CartDTO;
import com.dom.dto.CartItemDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Cart Service endpoints.
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private static final Logger log = LoggerFactory.getLogger(CartController.class);
    private final CartService cartService;

    /**
     * GET /api/cart/{userId} - Get user's cart.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<CartDTO>> getCart(@PathVariable Long userId) {
        log.info("GET /api/cart/{}", userId);
        CartDTO cart = cartService.getCart(userId);
        return ResponseEntity.ok(ApiResponse.ok(cart));
    }

    /**
     * POST /api/cart/{userId}/items - Add item to cart.
     */
    @PostMapping("/{userId}/items")
    public ResponseEntity<ApiResponse<CartDTO>> addItem(
            @PathVariable Long userId,
            @Valid @RequestBody CartItemDTO itemDTO) {
        log.info("POST /api/cart/{}/items - productId={}", userId, itemDTO.getProductId());
        CartDTO cart = cartService.addItem(userId, itemDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(cart));
    }

    /**
     * PUT /api/cart/{userId}/items/{itemId} - Update item quantity.
     */
    @PutMapping("/{userId}/items/{itemId}")
    public ResponseEntity<ApiResponse<CartDTO>> updateItem(
            @PathVariable Long userId,
            @PathVariable Long itemId,
            @RequestParam Integer quantity) {
        log.info("PUT /api/cart/{}/items/{} - quantity={}", userId, itemId, quantity);
        CartDTO cart = cartService.updateItemQuantity(userId, itemId, quantity);
        return ResponseEntity.ok(ApiResponse.ok(cart, "Cart updated"));
    }

    /**
     * DELETE /api/cart/{userId}/items/{itemId} - Remove item from cart.
     */
    @DeleteMapping("/{userId}/items/{itemId}")
    public ResponseEntity<ApiResponse<CartDTO>> removeItem(
            @PathVariable Long userId,
            @PathVariable Long itemId) {
        log.info("DELETE /api/cart/{}/items/{}", userId, itemId);
        CartDTO cart = cartService.removeItem(userId, itemId);
        return ResponseEntity.ok(ApiResponse.ok(cart, "Item removed from cart"));
    }

    /**
     * DELETE /api/cart/{userId} - Clear cart.
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> clearCart(@PathVariable Long userId) {
        log.info("DELETE /api/cart/{}", userId);
        cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Cart cleared"));
    }
}
