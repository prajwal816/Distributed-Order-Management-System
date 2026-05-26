package com.dom.order.client;

import com.dom.common.response.ApiResponse;
import com.dom.dto.CartDTO;
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
 * REST client for Cart Service with circuit breaker.
 */
@Component
public class CartServiceClient {

    private static final Logger log = LoggerFactory.getLogger(CartServiceClient.class);
    private final RestTemplate restTemplate;
    private final String cartServiceUrl;

    public CartServiceClient(
            @Value("${services.cart-service.url:http://cart-service:8082}") String cartServiceUrl) {
        this.restTemplate = new RestTemplate();
        this.cartServiceUrl = cartServiceUrl;
    }

    @CircuitBreaker(name = "cartService", fallbackMethod = "getCartFallback")
    @Retry(name = "cartService")
    public CartDTO getCart(Long userId) {
        log.info("Calling Cart Service for user {}", userId);
        String url = cartServiceUrl + "/api/cart/" + userId;
        ApiResponse<CartDTO> response = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<ApiResponse<CartDTO>>() {}
        ).getBody();

        if (response != null && response.isSuccess()) {
            return response.getData();
        }
        throw new RuntimeException("Failed to fetch cart for user " + userId);
    }

    @CircuitBreaker(name = "cartService", fallbackMethod = "clearCartFallback")
    public void clearCart(Long userId) {
        log.info("Clearing cart for user {} via Cart Service", userId);
        String url = cartServiceUrl + "/api/cart/" + userId;
        restTemplate.delete(url);
    }

    @SuppressWarnings("unused")
    private CartDTO getCartFallback(Long userId, Throwable t) {
        log.warn("Cart Service unavailable for user {}. Error: {}", userId, t.getMessage());
        throw new RuntimeException("Cart Service is currently unavailable. Please try again later.");
    }

    @SuppressWarnings("unused")
    private void clearCartFallback(Long userId, Throwable t) {
        log.warn("Failed to clear cart for user {}. Will retry later. Error: {}", userId, t.getMessage());
    }
}
