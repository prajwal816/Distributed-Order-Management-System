package com.dom.order.observer;

import com.dom.order.model.Order;
import com.dom.order.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Observer Pattern: Event object representing an order lifecycle event.
 */
@Data
@Builder
@AllArgsConstructor
public class OrderEvent {

    private final Order order;
    private final OrderStatus previousStatus;
    private final OrderStatus newStatus;

    @Builder.Default
    private final LocalDateTime timestamp = LocalDateTime.now();

    public enum EventType {
        ORDER_CREATED,
        ORDER_CONFIRMED,
        ORDER_SHIPPED,
        ORDER_DELIVERED,
        ORDER_CANCELLED
    }

    public EventType getEventType() {
        return switch (newStatus) {
            case PENDING -> EventType.ORDER_CREATED;
            case CONFIRMED, PROCESSING -> EventType.ORDER_CONFIRMED;
            case SHIPPED -> EventType.ORDER_SHIPPED;
            case DELIVERED -> EventType.ORDER_DELIVERED;
            case CANCELLED, REFUNDED -> EventType.ORDER_CANCELLED;
        };
    }
}
