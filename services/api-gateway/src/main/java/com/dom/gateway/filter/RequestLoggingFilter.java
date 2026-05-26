package com.dom.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Logging filter that records request/response details and tracks latency.
 * Adds correlation ID and response time headers.
 */
@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long startTime = System.currentTimeMillis();
        String correlationId = UUID.randomUUID().toString().substring(0, 8);
        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getURI().getPath();

        // Add correlation ID header
        exchange.getRequest().mutate()
                .header("X-Correlation-Id", correlationId)
                .build();

        log.info("[{}] → {} {} from {}",
                correlationId, method, path,
                exchange.getRequest().getRemoteAddress());

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            long duration = System.currentTimeMillis() - startTime;
            int status = exchange.getResponse().getStatusCode() != null
                    ? exchange.getResponse().getStatusCode().value() : 0;

            exchange.getResponse().getHeaders().add("X-Correlation-Id", correlationId);
            exchange.getResponse().getHeaders().add("X-Response-Time", duration + "ms");

            log.info("[{}] ← {} {} - {} ({}ms)",
                    correlationId, method, path, status, duration);

            // Performance warning
            if (duration > 200) {
                log.warn("[{}] ⚠ Slow response: {} {} took {}ms (exceeds 200ms SLA)",
                        correlationId, method, path, duration);
            }
        }));
    }

    @Override
    public int getOrder() {
        return -2; // Run before auth filter
    }
}
