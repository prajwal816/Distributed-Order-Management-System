package com.dom.product.service.strategy;

import com.dom.product.model.Product;
import com.dom.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Strategy Pattern: Search products within a price range.
 */
@Component
@RequiredArgsConstructor
public class PriceRangeSearchStrategy implements ProductSearchStrategy {

    private static final Logger log = LoggerFactory.getLogger(PriceRangeSearchStrategy.class);
    private final ProductRepository productRepository;

    @Override
    public String getType() {
        return "price_range";
    }

    @Override
    public List<Product> search(Map<String, String> params) {
        BigDecimal minPrice = new BigDecimal(params.getOrDefault("minPrice", "0"));
        BigDecimal maxPrice = new BigDecimal(params.getOrDefault("maxPrice", "999999"));
        log.info("Executing price range search strategy: {} - {}", minPrice, maxPrice);
        return productRepository.findByPriceRange(minPrice, maxPrice);
    }
}
