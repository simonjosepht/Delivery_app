package com.simon.application.mapper;

import com.simon.application.dto.response.DeliveryResponse;
import com.simon.application.entity.Delivery;

public class DeliveryMapper {

    private DeliveryMapper() {
    }

    public static Delivery toEntity(Long orderId) {

        return Delivery.builder()
                .orderId(orderId)
                .build();
    }

    public static DeliveryResponse toResponse(Delivery delivery) {

        return DeliveryResponse.builder()
                .id(delivery.getId())
                .orderId(delivery.getOrderId())
                .driverId(delivery.getDriverId())
                .status(delivery.getStatus())
                .createdAt(delivery.getCreatedAt())
                .updatedAt(delivery.getUpdatedAt())
                .build();
    }
}
