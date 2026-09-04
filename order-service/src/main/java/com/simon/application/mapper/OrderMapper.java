package com.simon.application.mapper;

import com.simon.application.dto.request.CreateOrderRequest;
import com.simon.application.dto.response.OrderResponse;
import com.simon.application.dto.response.OrderSummaryResponse;
import com.simon.application.entity.Order;

public class OrderMapper {

    private OrderMapper() {
    }

    public static Order toEntity(CreateOrderRequest request, Long customerId) {

        return Order.builder()
                .customerId(customerId)
                .itemDescription(request.getItemDescription())
                .quantity(request.getQuantity())
                .totalAmount(request.getTotalAmount())
                .deliveryAddress(request.getDeliveryAddress())
                .build();
    }

    public static OrderResponse toResponse(Order order) {

        return OrderResponse.builder()
                .id(order.getId())
                .customerId(order.getCustomerId())
                .itemDescription(order.getItemDescription())
                .quantity(order.getQuantity())
                .totalAmount(order.getTotalAmount())
                .deliveryAddress(order.getDeliveryAddress())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    public static OrderSummaryResponse toSummaryResponse(Order order) {

        return OrderSummaryResponse.builder()
                .id(order.getId())
                .status(order.getStatus())
                .build();
    }
}
