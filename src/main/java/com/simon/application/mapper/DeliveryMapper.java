package com.simon.application.mapper;

import com.simon.application.dto.response.DeliveryResponse;
import com.simon.application.entity.Delivery;
import com.simon.application.entity.Order;

public class DeliveryMapper {

    private DeliveryMapper() {
    }

    public static Delivery toEntity(Order order) {

        return Delivery.builder()
                .order(order)
                .build();
    }

    public static DeliveryResponse toResponse(Delivery delivery) {

        return DeliveryResponse.builder()
                .id(delivery.getId())
                .orderId(delivery.getOrder().getId())
                .driverId(delivery.getDriver() != null ? delivery.getDriver().getId() : null)
                .status(delivery.getStatus())
                .createdAt(delivery.getCreatedAt())
                .updatedAt(delivery.getUpdatedAt())
                .build();
    }
}
