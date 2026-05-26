package com.dom.order.service;

import com.dom.dto.OrderDTO;

import java.util.List;

/**
 * Order service interface for order management operations.
 */
public interface OrderService {

    OrderDTO createOrder(OrderDTO orderDTO);

    OrderDTO getOrderById(Long id);

    List<OrderDTO> getOrdersByUserId(Long userId);

    OrderDTO updateOrderStatus(Long id, String status);
}
