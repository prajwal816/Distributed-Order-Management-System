package com.dom.product.service.strategy;

import com.dom.product.model.Product;

import java.util.List;
import java.util.Map;

/**
 * Strategy Pattern: Interface for different product search strategies.
 * Each implementation encapsulates a specific search algorithm.
 */
public interface ProductSearchStrategy {

    /**
     * Returns the search type identifier (e.g., "name", "category", "price_range", "keyword").
     */
    String getType();

    /**
     * Execute the search with the given parameters.
     */
    List<Product> search(Map<String, String> params);
}
