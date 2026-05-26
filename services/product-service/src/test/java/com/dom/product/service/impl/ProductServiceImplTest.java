package com.dom.product.service.impl;

import com.dom.common.exception.ResourceNotFoundException;
import com.dom.dto.ProductDTO;
import com.dom.product.model.Product;
import com.dom.product.repository.ProductRepository;
import com.dom.product.service.strategy.CategorySearchStrategy;
import com.dom.product.service.strategy.NameSearchStrategy;
import com.dom.product.service.strategy.ProductSearchStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductServiceImpl Unit Tests")
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private NameSearchStrategy nameSearchStrategy;

    @Mock
    private CategorySearchStrategy categorySearchStrategy;

    private ProductServiceImpl productService;

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        List<ProductSearchStrategy> strategies = List.of(nameSearchStrategy, categorySearchStrategy);
        productService = new ProductServiceImpl(productRepository, strategies);

        sampleProduct = Product.builder()
                .id(1L)
                .name("MacBook Pro")
                .description("Apple laptop")
                .price(new BigDecimal("2499.99"))
                .category("ELECTRONICS")
                .active(true)
                .build();
    }

    @Test
    @DisplayName("getAllProducts returns list of active products")
    void getAllProducts_ReturnsActiveProducts() {
        when(productRepository.findByActiveTrue()).thenReturn(List.of(sampleProduct));

        List<ProductDTO> result = productService.getAllProducts();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("MacBook Pro");
        verify(productRepository).findByActiveTrue();
    }

    @Test
    @DisplayName("getProductById returns product when found")
    void getProductById_Found_ReturnsProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

        ProductDTO result = productService.getProductById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("MacBook Pro");
        assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("2499.99"));
    }

    @Test
    @DisplayName("getProductById throws ResourceNotFoundException when not found")
    void getProductById_NotFound_ThrowsException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found");
    }

    @Test
    @DisplayName("createProduct saves and returns new product")
    void createProduct_Success() {
        ProductDTO dto = ProductDTO.builder()
                .name("iPad Air")
                .description("Apple tablet")
                .price(new BigDecimal("599.99"))
                .category("ELECTRONICS")
                .build();

        Product saved = Product.builder()
                .id(2L)
                .name("iPad Air")
                .description("Apple tablet")
                .price(new BigDecimal("599.99"))
                .category("ELECTRONICS")
                .active(true)
                .build();

        when(productRepository.save(any(Product.class))).thenReturn(saved);

        ProductDTO result = productService.createProduct(dto);

        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getName()).isEqualTo("iPad Air");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("updateProduct updates existing product fields")
    void updateProduct_Success() {
        ProductDTO updateDto = ProductDTO.builder()
                .name("MacBook Pro 16 M3")
                .description("Updated laptop")
                .price(new BigDecimal("2999.99"))
                .category("ELECTRONICS")
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        ProductDTO result = productService.updateProduct(1L, updateDto);

        assertThat(result).isNotNull();
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("deleteProduct soft-deletes by setting active to false")
    void deleteProduct_SoftDeletes() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        productService.deleteProduct(1L);

        assertThat(sampleProduct.getActive()).isFalse();
        verify(productRepository).save(sampleProduct);
    }

    @Test
    @DisplayName("searchProducts uses correct strategy based on searchType")
    void searchProducts_UsesCorrectStrategy() {
        when(nameSearchStrategy.getType()).thenReturn("name");
        when(nameSearchStrategy.search(any())).thenReturn(List.of(sampleProduct));

        Map<String, String> params = Map.of("name", "MacBook");
        List<ProductDTO> result = productService.searchProducts("name", params);

        assertThat(result).hasSize(1);
        verify(nameSearchStrategy).search(params);
    }

    @Test
    @DisplayName("searchProducts throws exception for unknown search type")
    void searchProducts_UnknownType_ThrowsException() {
        when(nameSearchStrategy.getType()).thenReturn("name");
        when(categorySearchStrategy.getType()).thenReturn("category");

        assertThatThrownBy(() -> productService.searchProducts("unknown", Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown search type");
    }
}
