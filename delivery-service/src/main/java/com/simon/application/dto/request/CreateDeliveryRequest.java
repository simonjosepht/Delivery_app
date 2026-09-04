package com.simon.application.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateDeliveryRequest {

    @NotNull(message = "Order id is required")
    private Long orderId;
}
