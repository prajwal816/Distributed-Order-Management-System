package com.dom.order.controller;

import com.dom.common.response.ApiResponse;
import com.dom.dto.OrderDTO;
import com.dom.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for Order Service endpoints.
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);
    private final OrderService orderService;

    /**
     * POST /api/orders - Create an order from the user's cart.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<OrderDTO>> createOrder(
            @Valid @RequestBody OrderDTO orderDTO) {
        log.info("POST /api/orders - userId={}, type={}", orderDTO.getUserId(), orderDTO.getOrderType());
        OrderDTO created = orderService.createOrder(orderDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(created));
    }

    /**
     * GET /api/orders/{id} - Get order by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrder(@PathVariable Long id) {
        log.info("GET /api/orders/{}", id);
        OrderDTO order = orderService.getOrderById(id);
        return ResponseEntity.ok(ApiResponse.ok(order));
    }

    /**
     * GET /api/orders/user/{userId} - Get order history for a user.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getOrderHistory(@PathVariable Long userId) {
        log.info("GET /api/orders/user/{}", userId);
        List<OrderDTO> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(ApiResponse.ok(orders,
                String.format("Found %d orders", orders.size())));
    }

    /**
     * PUT /api/orders/{id}/status - Update order status.
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderDTO>> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String status = body.get("status");
        log.info("PUT /api/orders/{}/status - status={}", id, status);
        OrderDTO updated = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(ApiResponse.ok(updated, "Order status updated"));
    }

    /**
     * GET /api/orders/{id}/track - Track order (alias for getOrder with tracking info).
     */
    @GetMapping("/{id}/track")
    public ResponseEntity<ApiResponse<OrderDTO>> trackOrder(@PathVariable Long id) {
        log.info("GET /api/orders/{}/track", id);
        OrderDTO order = orderService.getOrderById(id);
        return ResponseEntity.ok(ApiResponse.ok(order, "Order tracking info"));
    }
}
