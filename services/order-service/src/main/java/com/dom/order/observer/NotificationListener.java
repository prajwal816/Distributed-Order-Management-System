package com.dom.order.observer;

import com.dom.order.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Observer Pattern: Listener that simulates sending notifications on order events.
 */
@Component
public class NotificationListener implements OrderEventListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationListener.class);

    @Override
    public void onOrderEvent(OrderEvent event) {
        Order order = event.getOrder();

        switch (event.getEventType()) {
            case ORDER_CREATED -> log.info(
                    "[Notification] 📧 Email sent to user #{}: Your order #{} has been placed! Total: ${}",
                    order.getUserId(), order.getId(), order.getTotalAmount());
            case ORDER_CONFIRMED -> log.info(
                    "[Notification] 📧 Email sent to user #{}: Your order #{} has been confirmed!",
                    order.getUserId(), order.getId());
            case ORDER_SHIPPED -> log.info(
                    "[Notification] 📱 SMS sent to user #{}: Your order #{} has been shipped!",
                    order.getUserId(), order.getId());
            case ORDER_DELIVERED -> log.info(
                    "[Notification] 📧 Email sent to user #{}: Your order #{} has been delivered! Please rate your experience.",
                    order.getUserId(), order.getId());
            case ORDER_CANCELLED -> log.info(
                    "[Notification] 📧 Email sent to user #{}: Your order #{} has been cancelled. Refund will be processed.",
                    order.getUserId(), order.getId());
        }
    }

    @Override
    public boolean supports(OrderEvent.EventType eventType) {
        return true; // Handles all event types
    }
}
