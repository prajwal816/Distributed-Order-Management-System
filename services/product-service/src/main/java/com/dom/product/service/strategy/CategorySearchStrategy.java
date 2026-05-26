package com.dom.product.service.strategy;

import com.dom.product.model.Product;
import com.dom.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Strategy Pattern: Search products by category.
 */
@Component
@RequiredArgsConstructor
public class CategorySearchStrategy implements ProductSearchStrategy {

    private static final Logger log = LoggerFactory.getLogger(CategorySearchStrategy.class);
    private final ProductRepository productRepository;

    @Override
    public String getType() {
        return "category";
    }

    @Override
    public List<Product> search(Map<String, String> params) {
        String category = params.get("category");
        log.info("Executing category search strategy for: {}", category);
        return productRepository.findByCategoryIgnoreCaseAndActiveTrue(category);
    }
}
