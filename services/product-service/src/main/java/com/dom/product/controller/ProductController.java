package com.dom.product.controller;

import com.dom.common.response.ApiResponse;
import com.dom.dto.ProductDTO;
import com.dom.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for Product Service endpoints.
 * Provides CRUD operations and search/filter capabilities.
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);
    private final ProductService productService;

    /**
     * GET /api/products - List all active products or search with filters.
     * Query params: searchType (name|category|price_range), name, category, minPrice, maxPrice
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getProducts(
            @RequestParam(required = false) String searchType,
            @RequestParam(required = false) Map<String, String> params) {

        log.info("GET /api/products - searchType={}, params={}", searchType, params);
        List<ProductDTO> products;

        if (searchType != null && !searchType.isBlank()) {
            products = productService.searchProducts(searchType, params);
        } else {
            products = productService.getAllProducts();
        }

        return ResponseEntity.ok(ApiResponse.ok(products,
                String.format("Found %d products", products.size())));
    }

    /**
     * GET /api/products/{id} - Get a product by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDTO>> getProduct(@PathVariable Long id) {
        log.info("GET /api/products/{}", id);
        ProductDTO product = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.ok(product));
    }

    /**
     * POST /api/products - Create a new product.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ProductDTO>> createProduct(
            @Valid @RequestBody ProductDTO productDTO) {
        log.info("POST /api/products - name={}", productDTO.getName());
        ProductDTO created = productService.createProduct(productDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(created));
    }

    /**
     * PUT /api/products/{id} - Update an existing product.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDTO>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductDTO productDTO) {
        log.info("PUT /api/products/{}", id);
        ProductDTO updated = productService.updateProduct(id, productDTO);
        return ResponseEntity.ok(ApiResponse.ok(updated, "Product updated successfully"));
    }

    /**
     * DELETE /api/products/{id} - Soft-delete a product.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        log.info("DELETE /api/products/{}", id);
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Product deleted successfully"));
    }
}
