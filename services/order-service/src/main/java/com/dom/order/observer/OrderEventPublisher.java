package com.dom.order.observer;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Observer Pattern: Publisher that dispatches order events to all registered listeners.
 */
@Component
@RequiredArgsConstructor
public class OrderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OrderEventPublisher.class);
    private final List<OrderEventListener> listeners;

    /**
     * Publish an order event to all supporting listeners.
     */
    public void publish(OrderEvent event) {
        log.info("Publishing order event: {} for order #{}", event.getEventType(), event.getOrder().getId());

        listeners.stream()
                .filter(listener -> listener.supports(event.getEventType()))
                .forEach(listener -> {
                    try {
                        log.debug("Dispatching to listener: {}", listener.getClass().getSimpleName());
                        listener.onOrderEvent(event);
                    } catch (Exception e) {
                        log.error("Error in listener {}: {}", listener.getClass().getSimpleName(), e.getMessage(), e);
                    }
                });
    }
}
