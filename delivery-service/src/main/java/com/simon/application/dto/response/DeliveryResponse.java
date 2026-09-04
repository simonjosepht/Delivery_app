package com.simon.application.dto.response;

import com.simon.application.enums.DeliveryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryResponse {

    private Long id;

    private Long orderId;

    private Long driverId;

    private DeliveryStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
