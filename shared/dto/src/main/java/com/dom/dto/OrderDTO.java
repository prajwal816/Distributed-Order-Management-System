package com.dom.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for Order entity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderDTO implements Serializable {

    private Long id;

    @NotNull(message = "User ID is required")
    private Long userId;

    private List<OrderItemDTO> items;

    private BigDecimal totalAmount;

    private String status;

    @NotBlank(message = "Shipping address is required")
    @Size(max = 500, message = "Shipping address is too long")
    private String shippingAddress;

    private String orderType;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
