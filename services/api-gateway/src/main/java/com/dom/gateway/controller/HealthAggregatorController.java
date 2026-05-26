package com.dom.gateway.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Health aggregator controller that checks all downstream services.
 */
@RestController
public class HealthAggregatorController {

    private static final Logger log = LoggerFactory.getLogger(HealthAggregatorController.class);
    private final WebClient webClient = WebClient.create();

    @Value("${services.product-service.url:http://product-service:8081}")
    private String productServiceUrl;

    @Value("${services.cart-service.url:http://cart-service:8082}")
    private String cartServiceUrl;

    @Value("${services.order-service.url:http://order-service:8083}")
    private String orderServiceUrl;

    @Value("${services.inventory-service.url:http://inventory-service:8084}")
    private String inventoryServiceUrl;

    /**
     * GET /health - Aggregated health status of all services.
     */
    @GetMapping("/health")
    public Mono<ResponseEntity<Map<String, Object>>> healthCheck() {
        log.info("Performing aggregated health check");

        Map<String, Object> health = new HashMap<>();
        health.put("gateway", "UP");
        health.put("timestamp", LocalDateTime.now().toString());

        return Mono.zip(
                checkService("product-service", productServiceUrl),
                checkService("cart-service", cartServiceUrl),
                checkService("order-service", orderServiceUrl),
                checkService("inventory-service", inventoryServiceUrl)
        ).map(tuple -> {
            health.put("product-service", tuple.getT1());
            health.put("cart-service", tuple.getT2());
            health.put("order-service", tuple.getT3());
            health.put("inventory-service", tuple.getT4());

            boolean allUp = tuple.getT1().equals("UP") && tuple.getT2().equals("UP")
                    && tuple.getT3().equals("UP") && tuple.getT4().equals("UP");
            health.put("status", allUp ? "UP" : "DEGRADED");

            return ResponseEntity.ok(health);
        }).onErrorReturn(ResponseEntity.ok(Map.of(
                "gateway", "UP",
                "status", "DEGRADED",
                "timestamp", LocalDateTime.now().toString()
        )));
    }

    private Mono<String> checkService(String name, String url) {
        return webClient.get()
                .uri(url + "/actuator/health")
                .retrieve()
                .bodyToMono(String.class)
                .map(body -> "UP")
                .timeout(java.time.Duration.ofSeconds(3))
                .onErrorReturn("DOWN")
                .doOnNext(status -> log.debug("{}: {}", name, status));
    }
}
