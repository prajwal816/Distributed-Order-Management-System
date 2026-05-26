package com.dom.order.factory;

import com.dom.dto.CartDTO;
import com.dom.dto.CartItemDTO;
import com.dom.order.model.Order;
import com.dom.order.model.OrderType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("OrderFactory Unit Tests (Factory Pattern)")
class OrderFactoryTest {

    private OrderFactory orderFactory;
    private CartDTO sampleCart;

    @BeforeEach
    void setUp() {
        orderFactory = new OrderFactory();
        sampleCart = CartDTO.builder()
                .userId(1L)
                .items(List.of(
                        CartItemDTO.builder()
                                .productId(1L)
                                .productName("MacBook Pro")
                                .price(new BigDecimal("2499.99"))
                                .quantity(1)
                                .build(),
                        CartItemDTO.builder()
                                .productId(2L)
                                .productName("iPhone 15")
                                .price(new BigDecimal("1199.99"))
                                .quantity(2)
                                .build()
                ))
                .build();
    }

    @Test
    @DisplayName("STANDARD order has no surcharge or discount")
    void createOrder_Standard_NoModifier() {
        Order order = orderFactory.createOrder(sampleCart, "123 Main St", "STANDARD");

        assertThat(order.getOrderType()).isEqualTo(OrderType.STANDARD);
        assertThat(order.getItems()).hasSize(2);
        // 2499.99 + (1199.99 * 2) = 4899.97
        assertThat(order.getTotalAmount()).isEqualByComparingTo(new BigDecimal("4899.97"));
    }

    @Test
    @DisplayName("EXPRESS order adds 15% surcharge")
    void createOrder_Express_AddsSurcharge() {
        Order order = orderFactory.createOrder(sampleCart, "123 Main St", "EXPRESS");

        assertThat(order.getOrderType()).isEqualTo(OrderType.EXPRESS);
        // 4899.97 * 1.15 = 5634.97 (rounded)
        assertThat(order.getTotalAmount()).isEqualByComparingTo(new BigDecimal("5634.97"));
    }

    @Test
    @DisplayName("BULK order with 5+ items gets 10% discount")
    void createOrder_Bulk_WithDiscount() {
        CartDTO bulkCart = CartDTO.builder()
                .userId(1L)
                .items(List.of(
                        CartItemDTO.builder().productId(1L).productName("Item1")
                                .price(new BigDecimal("100.00")).quantity(3).build(),
                        CartItemDTO.builder().productId(2L).productName("Item2")
                                .price(new BigDecimal("50.00")).quantity(3).build()
                ))
                .build();

        Order order = orderFactory.createOrder(bulkCart, "123 Main St", "BULK");

        assertThat(order.getOrderType()).isEqualTo(OrderType.BULK);
        // (100*3 + 50*3) = 450 * 0.90 = 405.00
        assertThat(order.getTotalAmount()).isEqualByComparingTo(new BigDecimal("405.00"));
    }

    @Test
    @DisplayName("BULK order under threshold gets no discount")
    void createOrder_Bulk_NoDiscount_UnderThreshold() {
        CartDTO smallCart = CartDTO.builder()
                .userId(1L)
                .items(List.of(
                        CartItemDTO.builder().productId(1L).productName("Item1")
                                .price(new BigDecimal("100.00")).quantity(2).build()
                ))
                .build();

        Order order = orderFactory.createOrder(smallCart, "123 Main St", "BULK");

        // 2 items < 5 threshold, no discount
        assertThat(order.getTotalAmount()).isEqualByComparingTo(new BigDecimal("200.00"));
    }

    @Test
    @DisplayName("Unknown order type defaults to STANDARD")
    void createOrder_UnknownType_DefaultsToStandard() {
        Order order = orderFactory.createOrder(sampleCart, "123 Main St", "PREMIUM");

        assertThat(order.getOrderType()).isEqualTo(OrderType.STANDARD);
    }

    @Test
    @DisplayName("Null order type defaults to STANDARD")
    void createOrder_NullType_DefaultsToStandard() {
        Order order = orderFactory.createOrder(sampleCart, "123 Main St", null);

        assertThat(order.getOrderType()).isEqualTo(OrderType.STANDARD);
    }
}
