package com.dom.common.constants;

/**
 * Shared constants used across all microservices.
 */
public final class AppConstants {

    private AppConstants() {
    }

    // Service names
    public static final String PRODUCT_SERVICE = "product-service";
    public static final String CART_SERVICE = "cart-service";
    public static final String ORDER_SERVICE = "order-service";
    public static final String INVENTORY_SERVICE = "inventory-service";

    // Default pagination
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    // Cache keys
    public static final String CACHE_PRODUCTS = "products";
    public static final String CACHE_PRODUCT_BY_ID = "product";
    public static final String CACHE_CART = "cart";
    public static final String CACHE_INVENTORY = "inventory";

    // Headers
    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_CORRELATION_ID = "X-Correlation-Id";
}
