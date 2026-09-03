package com.simon.application.mapper;

import com.simon.application.dto.request.CreateOrderRequest;
import com.simon.application.dto.response.OrderResponse;
import com.simon.application.entity.Order;
import com.simon.application.entity.User;

public class OrderMapper {

    private OrderMapper() {
    }

    public static Order toEntity(CreateOrderRequest request, User customer) {

        return Order.builder()
                .customer(customer)
                .itemDescription(request.getItemDescription())
                .quantity(request.getQuantity())
                .totalAmount(request.getTotalAmount())
                .deliveryAddress(request.getDeliveryAddress())
                .build();
    }

    public static OrderResponse toResponse(Order order) {

        return OrderResponse.builder()
                .id(order.getId())
                .customerId(order.getCustomer().getId())
                .itemDescription(order.getItemDescription())
                .quantity(order.getQuantity())
                .totalAmount(order.getTotalAmount())
                .deliveryAddress(order.getDeliveryAddress())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
