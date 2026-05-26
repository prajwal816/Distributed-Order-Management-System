package com.dom.order.observer;

import com.dom.order.client.InventoryServiceClient;
import com.dom.order.model.Order;
import com.dom.order.model.OrderItem;
import com.dom.order.model.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Observer Pattern Unit Tests")
class OrderEventPublisherTest {

    @Mock
    private InventoryServiceClient inventoryClient;

    private OrderEventPublisher publisher;
    private InventoryUpdateListener inventoryListener;
    private NotificationListener notificationListener;

    private Order sampleOrder;

    @BeforeEach
    void setUp() {
        inventoryListener = new InventoryUpdateListener(inventoryClient);
        notificationListener = new NotificationListener();
        publisher = new OrderEventPublisher(List.of(inventoryListener, notificationListener));

        OrderItem item = OrderItem.builder()
                .productId(1L)
                .productName("MacBook")
                .price(new BigDecimal("2499.99"))
                .quantity(1)
                .build();

        sampleOrder = Order.builder()
                .id(100L)
                .userId(1L)
                .totalAmount(new BigDecimal("2499.99"))
                .status(OrderStatus.PENDING)
                .items(List.of(item))
                .build();
    }

    @Test
    @DisplayName("ORDER_CREATED event triggers inventory reservation")
    void publish_OrderCreated_TriggersInventoryReservation() {
        OrderEvent event = OrderEvent.builder()
                .order(sampleOrder)
                .previousStatus(null)
                .newStatus(OrderStatus.PENDING)
                .build();

        publisher.publish(event);

        verify(inventoryClient).reserveStock(1L, 1, 100L);
    }

    @Test
    @DisplayName("ORDER_CANCELLED event triggers inventory release")
    void publish_OrderCancelled_TriggersInventoryRelease() {
        OrderEvent event = OrderEvent.builder()
                .order(sampleOrder)
                .previousStatus(OrderStatus.PENDING)
                .newStatus(OrderStatus.CANCELLED)
                .build();

        publisher.publish(event);

        verify(inventoryClient).releaseStock(1L, 1, 100L);
    }

    @Test
    @DisplayName("InventoryUpdateListener supports ORDER_CREATED and ORDER_CANCELLED")
    void inventoryListener_SupportsCorrectEvents() {
        assertThat(inventoryListener.supports(OrderEvent.EventType.ORDER_CREATED)).isTrue();
        assertThat(inventoryListener.supports(OrderEvent.EventType.ORDER_CANCELLED)).isTrue();
        assertThat(inventoryListener.supports(OrderEvent.EventType.ORDER_SHIPPED)).isFalse();
    }

    @Test
    @DisplayName("NotificationListener supports all event types")
    void notificationListener_SupportsAllEvents() {
        for (OrderEvent.EventType type : OrderEvent.EventType.values()) {
            assertThat(notificationListener.supports(type)).isTrue();
        }
    }
}
