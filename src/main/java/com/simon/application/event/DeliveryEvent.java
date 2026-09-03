package com.simon.application.event;

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
public class DeliveryEvent {

    private EventType eventType;

    private Long deliveryId;

    private Long orderId;

    private Long driverId;

    private DeliveryStatus status;

    private LocalDateTime occurredAt;
}
