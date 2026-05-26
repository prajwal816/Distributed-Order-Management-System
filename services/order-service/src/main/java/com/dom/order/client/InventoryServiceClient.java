package com.dom.order.client;

import com.dom.common.response.ApiResponse;
import com.dom.dto.InventoryDTO;
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
 * REST client for Inventory Service with circuit breaker and retry.
 */
@Component
public class InventoryServiceClient {

    private static final Logger log = LoggerFactory.getLogger(InventoryServiceClient.class);
    private final RestTemplate restTemplate;
    private final String inventoryServiceUrl;

    public InventoryServiceClient(
            @Value("${services.inventory-service.url:http://inventory-service:8084}") String inventoryServiceUrl) {
        this.restTemplate = new RestTemplate();
        this.inventoryServiceUrl = inventoryServiceUrl;
    }

    @CircuitBreaker(name = "inventoryService", fallbackMethod = "reserveStockFallback")
    @Retry(name = "inventoryService")
    public InventoryDTO reserveStock(Long productId, int quantity, Long orderId) {
        log.info("Reserving {} units of product {} for order {}", quantity, productId, orderId);
        String url = String.format("%s/api/inventory/%d/reserve?quantity=%d&orderId=%d",
                inventoryServiceUrl, productId, quantity, orderId);
        ApiResponse<InventoryDTO> response = restTemplate.exchange(
                url, HttpMethod.POST, null,
                new ParameterizedTypeReference<ApiResponse<InventoryDTO>>() {}
        ).getBody();

        if (response != null && response.isSuccess()) {
            return response.getData();
        }
        throw new RuntimeException("Failed to reserve stock for product " + productId);
    }

    @CircuitBreaker(name = "inventoryService", fallbackMethod = "releaseStockFallback")
    public InventoryDTO releaseStock(Long productId, int quantity, Long orderId) {
        log.info("Releasing {} units of product {} for order {}", quantity, productId, orderId);
        String url = String.format("%s/api/inventory/%d/release?quantity=%d&orderId=%d",
                inventoryServiceUrl, productId, quantity, orderId);
        ApiResponse<InventoryDTO> response = restTemplate.exchange(
                url, HttpMethod.POST, null,
                new ParameterizedTypeReference<ApiResponse<InventoryDTO>>() {}
        ).getBody();

        if (response != null && response.isSuccess()) {
            return response.getData();
        }
        throw new RuntimeException("Failed to release stock for product " + productId);
    }

    @SuppressWarnings("unused")
    private InventoryDTO reserveStockFallback(Long productId, int quantity, Long orderId, Throwable t) {
        log.error("Inventory Service unavailable. Cannot reserve stock for product {}. Error: {}",
                productId, t.getMessage());
        throw new RuntimeException("Inventory Service is currently unavailable. Cannot process order.");
    }

    @SuppressWarnings("unused")
    private InventoryDTO releaseStockFallback(Long productId, int quantity, Long orderId, Throwable t) {
        log.error("Inventory Service unavailable. Cannot release stock for product {}. Error: {}",
                productId, t.getMessage());
        return null;
    }
}
