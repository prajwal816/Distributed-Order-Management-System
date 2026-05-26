package com.dom.order.service.impl;

import com.dom.common.exception.BadRequestException;
import com.dom.common.exception.ResourceNotFoundException;
import com.dom.dto.CartDTO;
import com.dom.dto.OrderDTO;
import com.dom.dto.OrderItemDTO;
import com.dom.order.client.CartServiceClient;
import com.dom.order.factory.OrderFactory;
import com.dom.order.model.Order;
import com.dom.order.model.OrderStatus;
import com.dom.order.observer.OrderEvent;
import com.dom.order.observer.OrderEventPublisher;
import com.dom.order.repository.OrderRepository;
import com.dom.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Order service implementation integrating Factory Pattern for order creation
 * and Observer Pattern for order lifecycle events.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final CartServiceClient cartServiceClient;
    private final OrderFactory orderFactory;
    private final OrderEventPublisher eventPublisher;

    @Override
    public OrderDTO createOrder(OrderDTO orderDTO) {
        log.info("Creating order for user {}", orderDTO.getUserId());

        // 1. Fetch cart from Cart Service
        CartDTO cart = cartServiceClient.getCart(orderDTO.getUserId());
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new BadRequestException("Cannot create order: cart is empty for user " + orderDTO.getUserId());
        }

        // 2. Factory Pattern: Create order based on type (STANDARD, EXPRESS, BULK)
        Order order = orderFactory.createOrder(
                cart,
                orderDTO.getShippingAddress(),
                orderDTO.getOrderType()
        );

        // 3. Save order
        order = orderRepository.save(order);
        log.info("Order #{} created successfully. Total: ${}", order.getId(), order.getTotalAmount());

        // 4. Observer Pattern: Publish ORDER_CREATED event
        //    → InventoryUpdateListener reserves stock
        //    → NotificationListener sends confirmation email
        eventPublisher.publish(OrderEvent.builder()
                .order(order)
                .previousStatus(null)
                .newStatus(OrderStatus.PENDING)
                .build());

        // 5. Clear the cart after successful order
        try {
            cartServiceClient.clearCart(orderDTO.getUserId());
            log.info("Cart cleared for user {}", orderDTO.getUserId());
        } catch (Exception e) {
            log.warn("Failed to clear cart after order creation: {}", e.getMessage());
        }

        return toDTO(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Long id) {
        log.info("Fetching order #{}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
        return toDTO(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersByUserId(Long userId) {
        log.info("Fetching order history for user {}", userId);
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public OrderDTO updateOrderStatus(Long id, String status) {
        log.info("Updating order #{} status to {}", id, status);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));

        OrderStatus previousStatus = order.getStatus();
        OrderStatus newStatus;
        try {
            newStatus = OrderStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid order status: " + status);
        }

        validateStatusTransition(previousStatus, newStatus);
        order.setStatus(newStatus);
        order = orderRepository.save(order);

        // Observer Pattern: Publish status change event
        eventPublisher.publish(OrderEvent.builder()
                .order(order)
                .previousStatus(previousStatus)
                .newStatus(newStatus)
                .build());

        log.info("Order #{} status changed: {} → {}", id, previousStatus, newStatus);
        return toDTO(order);
    }

    private void validateStatusTransition(OrderStatus from, OrderStatus to) {
        // Define valid transitions
        boolean valid = switch (from) {
            case PENDING -> to == OrderStatus.CONFIRMED || to == OrderStatus.CANCELLED;
            case CONFIRMED -> to == OrderStatus.PROCESSING || to == OrderStatus.CANCELLED;
            case PROCESSING -> to == OrderStatus.SHIPPED || to == OrderStatus.CANCELLED;
            case SHIPPED -> to == OrderStatus.DELIVERED;
            case DELIVERED -> to == OrderStatus.REFUNDED;
            case CANCELLED, REFUNDED -> false;
        };

        if (!valid) {
            throw new BadRequestException(
                    String.format("Invalid status transition: %s → %s", from, to));
        }
    }

    private OrderDTO toDTO(Order order) {
        var items = order.getItems().stream()
                .map(item -> OrderItemDTO.builder()
                        .id(item.getId())
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .price(item.getPrice())
                        .quantity(item.getQuantity())
                        .subtotal(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .build())
                .collect(Collectors.toList());

        return OrderDTO.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .items(items)
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name())
                .orderType(order.getOrderType().name())
                .shippingAddress(order.getShippingAddress())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
