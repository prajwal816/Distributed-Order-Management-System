package com.dom.cart.client;

import com.dom.common.response.ApiResponse;
import com.dom.dto.ProductDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * REST client for Product Service with circuit breaker and retry mechanisms.
 */
@Component
public class ProductServiceClient {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceClient.class);
    private final RestTemplate restTemplate;
    private final String productServiceUrl;

    public ProductServiceClient(
            @Value("${services.product-service.url:http://product-service:8081}") String productServiceUrl) {
        this.restTemplate = new RestTemplate();
        this.productServiceUrl = productServiceUrl;
    }

    @CircuitBreaker(name = "productService", fallbackMethod = "getProductFallback")
    @Retry(name = "productService")
    public ProductDTO getProduct(Long productId) {
        log.info("Calling Product Service for product {}", productId);
        String url = productServiceUrl + "/api/products/" + productId;
        ApiResponse<ProductDTO> response = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<ApiResponse<ProductDTO>>() {}
        ).getBody();

        if (response != null && response.isSuccess()) {
            return response.getData();
        }
        throw new RuntimeException("Failed to fetch product " + productId);
    }

    /**
     * Fallback when Product Service is unavailable.
     */
    @SuppressWarnings("unused")
    private ProductDTO getProductFallback(Long productId, Throwable t) {
        log.warn("Product Service unavailable. Using fallback for product {}. Error: {}", productId, t.getMessage());
        return ProductDTO.builder()
                .id(productId)
                .name("Product #" + productId + " (cached)")
                .price(java.math.BigDecimal.ZERO)
                .category("UNKNOWN")
                .build();
    }
}
