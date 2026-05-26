package com.dom.product.service;

import com.dom.dto.ProductDTO;

import java.util.List;
import java.util.Map;

/**
 * Product service interface defining business operations.
 */
public interface ProductService {

    List<ProductDTO> getAllProducts();

    ProductDTO getProductById(Long id);

    ProductDTO createProduct(ProductDTO productDTO);

    ProductDTO updateProduct(Long id, ProductDTO productDTO);

    void deleteProduct(Long id);

    List<ProductDTO> searchProducts(String searchType, Map<String, String> params);
}
