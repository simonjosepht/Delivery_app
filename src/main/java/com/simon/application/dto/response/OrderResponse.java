package com.simon.application.dto.response;

import com.simon.application.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class OrderResponse {

    private Long id;

    private Long customerId;

    private String itemDescription;

    private Integer quantity;

    private BigDecimal totalAmount;

    private String deliveryAddress;

    private OrderStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
