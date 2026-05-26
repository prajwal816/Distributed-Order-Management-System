package com.dom.order.factory;

import com.dom.dto.CartDTO;
import com.dom.dto.CartItemDTO;
import com.dom.order.model.Order;
import com.dom.order.model.OrderItem;
import com.dom.order.model.OrderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Factory Pattern: Creates Order objects from cart data with different
 * creation logic based on order type (STANDARD, EXPRESS, BULK).
 *
 * - STANDARD: Normal pricing
 * - EXPRESS: 15% expedited shipping surcharge
 * - BULK: 10% discount for large orders (5+ total items)
 */
@Component
public class OrderFactory {

    private static final Logger log = LoggerFactory.getLogger(OrderFactory.class);

    private static final BigDecimal EXPRESS_SURCHARGE = new BigDecimal("1.15");
    private static final BigDecimal BULK_DISCOUNT = new BigDecimal("0.90");
    private static final int BULK_THRESHOLD = 5;

    /**
     * Create an Order from cart data with the specified order type.
     */
    public Order createOrder(CartDTO cart, String shippingAddress, String orderTypeStr) {
        OrderType orderType = parseOrderType(orderTypeStr);
        log.info("Factory creating {} order for user {}", orderType, cart.getUserId());

        Order order = Order.builder()
                .userId(cart.getUserId())
                .shippingAddress(shippingAddress)
                .orderType(orderType)
                .build();

        // Add items from cart
        for (CartItemDTO cartItem : cart.getItems()) {
            OrderItem orderItem = OrderItem.builder()
                    .productId(cartItem.getProductId())
                    .productName(cartItem.getProductName())
                    .price(cartItem.getPrice())
                    .quantity(cartItem.getQuantity())
                    .build();
            order.addItem(orderItem);
        }

        // Calculate total based on order type
        BigDecimal total = calculateTotal(order, orderType);
        order.setTotalAmount(total);

        log.info("Factory created {} order. Items: {}, Total: ${}", orderType, order.getItems().size(), total);
        return order;
    }

    private BigDecimal calculateTotal(Order order, OrderType orderType) {
        BigDecimal subtotal = order.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return switch (orderType) {
            case EXPRESS -> {
                BigDecimal total = subtotal.multiply(EXPRESS_SURCHARGE).setScale(2, RoundingMode.HALF_UP);
                log.info("EXPRESS order: subtotal=${}, surcharge=15%, total=${}", subtotal, total);
                yield total;
            }
            case BULK -> {
                int totalItems = order.getItems().stream().mapToInt(OrderItem::getQuantity).sum();
                if (totalItems >= BULK_THRESHOLD) {
                    BigDecimal total = subtotal.multiply(BULK_DISCOUNT).setScale(2, RoundingMode.HALF_UP);
                    log.info("BULK order: subtotal=${}, discount=10%, total=${}", subtotal, total);
                    yield total;
                }
                log.info("BULK order below threshold ({}), no discount applied. Total: ${}", totalItems, subtotal);
                yield subtotal;
            }
            case STANDARD -> {
                log.info("STANDARD order: total=${}", subtotal);
                yield subtotal;
            }
        };
    }

    private OrderType parseOrderType(String type) {
        if (type == null || type.isBlank()) {
            return OrderType.STANDARD;
        }
        try {
            return OrderType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Unknown order type '{}', defaulting to STANDARD", type);
            return OrderType.STANDARD;
        }
    }
}
