package com.dom.order.observer;

import com.dom.order.client.InventoryServiceClient;
import com.dom.order.model.Order;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Observer Pattern: Listener that updates inventory when orders are created or cancelled.
 */
@Component
@RequiredArgsConstructor
public class InventoryUpdateListener implements OrderEventListener {

    private static final Logger log = LoggerFactory.getLogger(InventoryUpdateListener.class);
    private final InventoryServiceClient inventoryClient;

    @Override
    public void onOrderEvent(OrderEvent event) {
        Order order = event.getOrder();

        switch (event.getEventType()) {
            case ORDER_CREATED -> {
                log.info("[Observer] Reserving inventory for order #{}", order.getId());
                order.getItems().forEach(item -> {
                    try {
                        inventoryClient.reserveStock(item.getProductId(), item.getQuantity(), order.getId());
                        log.info("[Observer] Reserved {} units of product #{}", item.getQuantity(), item.getProductId());
                    } catch (Exception e) {
                        log.error("[Observer] Failed to reserve stock for product #{}: {}",
                                item.getProductId(), e.getMessage());
                    }
                });
            }
            case ORDER_CANCELLED -> {
                log.info("[Observer] Releasing inventory for cancelled order #{}", order.getId());
                order.getItems().forEach(item -> {
                    try {
                        inventoryClient.releaseStock(item.getProductId(), item.getQuantity(), order.getId());
                        log.info("[Observer] Released {} units of product #{}", item.getQuantity(), item.getProductId());
                    } catch (Exception e) {
                        log.error("[Observer] Failed to release stock for product #{}: {}",
                                item.getProductId(), e.getMessage());
                    }
                });
            }
            default -> log.debug("[Observer] No inventory action for event: {}", event.getEventType());
        }
    }

    @Override
    public boolean supports(OrderEvent.EventType eventType) {
        return eventType == OrderEvent.EventType.ORDER_CREATED
                || eventType == OrderEvent.EventType.ORDER_CANCELLED;
    }
}
