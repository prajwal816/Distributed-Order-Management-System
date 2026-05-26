package com.dom.product.service.impl;

import com.dom.common.exception.ResourceNotFoundException;
import com.dom.dto.ProductDTO;
import com.dom.product.model.Product;
import com.dom.product.repository.ProductRepository;
import com.dom.product.service.ProductService;
import com.dom.product.service.strategy.ProductSearchStrategy;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Product service implementation with Redis caching and Strategy Pattern for search.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductRepository productRepository;
    private final List<ProductSearchStrategy> searchStrategies;

    @Override
    @Cacheable(value = "products", key = "'all'")
    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        log.info("Fetching all products from database (cache miss)");
        return productRepository.findByActiveTrue().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "product", key = "#id")
    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        log.info("Fetching product {} from database (cache miss)", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        return toDTO(product);
    }

    @Override
    @CacheEvict(value = "products", allEntries = true)
    public ProductDTO createProduct(ProductDTO dto) {
        log.info("Creating new product: {}", dto.getName());
        Product product = toEntity(dto);
        product = productRepository.save(product);
        log.info("Product created with id: {}", product.getId());
        return toDTO(product);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "products", allEntries = true),
            @CacheEvict(value = "product", key = "#id")
    })
    public ProductDTO updateProduct(Long id, ProductDTO dto) {
        log.info("Updating product: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setCategory(dto.getCategory());
        if (dto.getImageUrl() != null) {
            product.setImageUrl(dto.getImageUrl());
        }
        if (dto.getActive() != null) {
            product.setActive(dto.getActive());
        }

        product = productRepository.save(product);
        log.info("Product {} updated successfully", id);
        return toDTO(product);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "products", allEntries = true),
            @CacheEvict(value = "product", key = "#id")
    })
    public void deleteProduct(Long id) {
        log.info("Soft-deleting product: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        product.setActive(false);
        productRepository.save(product);
        log.info("Product {} deleted (soft)", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> searchProducts(String searchType, Map<String, String> params) {
        log.info("Searching products with strategy: {}, params: {}", searchType, params);

        // Strategy Pattern: Select the appropriate search strategy at runtime
        ProductSearchStrategy strategy = searchStrategies.stream()
                .filter(s -> s.getType().equalsIgnoreCase(searchType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown search type: " + searchType + ". Available: name, category, price_range"));

        return strategy.search(params).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // --- Mapping helpers ---

    private ProductDTO toDTO(Product product) {
        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(product.getCategory())
                .imageUrl(product.getImageUrl())
                .active(product.getActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    private Product toEntity(ProductDTO dto) {
        return Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .category(dto.getCategory())
                .imageUrl(dto.getImageUrl())
                .active(dto.getActive() != null ? dto.getActive() : true)
                .build();
    }
}
