package com.dom.order.observer;

/**
 * Observer Pattern: Listener interface for order events.
 * Implementations react to order lifecycle changes.
 */
public interface OrderEventListener {

    /**
     * Called when an order event occurs.
     */
    void onOrderEvent(OrderEvent event);

    /**
     * Returns true if this listener handles the given event type.
     */
    boolean supports(OrderEvent.EventType eventType);
}
