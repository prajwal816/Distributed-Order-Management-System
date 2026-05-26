package com.dom.cart.service.impl;

import com.dom.cart.client.ProductServiceClient;
import com.dom.cart.model.Cart;
import com.dom.cart.model.CartItem;
import com.dom.cart.repository.CartItemRepository;
import com.dom.cart.repository.CartRepository;
import com.dom.cart.service.CartService;
import com.dom.common.exception.ResourceNotFoundException;
import com.dom.dto.CartDTO;
import com.dom.dto.CartItemDTO;
import com.dom.dto.ProductDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.stream.Collectors;

/**
 * Cart service implementation with Redis caching and Product Service integration.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private static final Logger log = LoggerFactory.getLogger(CartServiceImpl.class);

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductServiceClient productServiceClient;

    @Override
    @Cacheable(value = "cart", key = "#userId")
    @Transactional(readOnly = true)
    public CartDTO getCart(Long userId) {
        log.info("Fetching cart for user {} (cache miss)", userId);
        Cart cart = getOrCreateCart(userId);
        return toDTO(cart);
    }

    @Override
    @CacheEvict(value = "cart", key = "#userId")
    public CartDTO addItem(Long userId, CartItemDTO itemDTO) {
        log.info("Adding item to cart for user {}: productId={}", userId, itemDTO.getProductId());
        Cart cart = getOrCreateCart(userId);

        // Fetch product details from Product Service (with circuit breaker)
        ProductDTO product = productServiceClient.getProduct(itemDTO.getProductId());

        // Check if product already in cart
        CartItem existingItem = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(itemDTO.getProductId()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + itemDTO.getQuantity());
            log.info("Updated existing item quantity to {}", existingItem.getQuantity());
        } else {
            CartItem newItem = CartItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .price(product.getPrice())
                    .quantity(itemDTO.getQuantity())
                    .build();
            cart.addItem(newItem);
            log.info("Added new item: {}", product.getName());
        }

        cart = cartRepository.save(cart);
        return toDTO(cart);
    }

    @Override
    @CacheEvict(value = "cart", key = "#userId")
    public CartDTO updateItemQuantity(Long userId, Long itemId, Integer quantity) {
        log.info("Updating item {} quantity to {} for user {}", itemId, quantity, userId);
        Cart cart = getOrCreateCart(userId);

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", itemId));

        if (quantity <= 0) {
            cart.removeItem(item);
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(quantity);
        }

        cart = cartRepository.save(cart);
        return toDTO(cart);
    }

    @Override
    @CacheEvict(value = "cart", key = "#userId")
    public CartDTO removeItem(Long userId, Long itemId) {
        log.info("Removing item {} from cart for user {}", itemId, userId);
        Cart cart = getOrCreateCart(userId);

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", itemId));

        cart.removeItem(item);
        cartItemRepository.delete(item);
        cart = cartRepository.save(cart);
        return toDTO(cart);
    }

    @Override
    @CacheEvict(value = "cart", key = "#userId")
    public void clearCart(Long userId) {
        log.info("Clearing cart for user {}", userId);
        Cart cart = getOrCreateCart(userId);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.info("Creating new cart for user {}", userId);
                    Cart newCart = Cart.builder().userId(userId).build();
                    return cartRepository.save(newCart);
                });
    }

    private CartDTO toDTO(Cart cart) {
        var items = cart.getItems().stream()
                .map(item -> CartItemDTO.builder()
                        .id(item.getId())
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .price(item.getPrice())
                        .quantity(item.getQuantity())
                        .subtotal(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .build())
                .collect(Collectors.toList());

        BigDecimal total = items.stream()
                .map(CartItemDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartDTO.builder()
                .id(cart.getId())
                .userId(cart.getUserId())
                .items(items)
                .totalAmount(total)
                .totalItems(items.size())
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }
}
