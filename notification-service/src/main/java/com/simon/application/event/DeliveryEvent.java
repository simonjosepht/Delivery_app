package com.simon.application.event;

import com.simon.application.enums.DeliveryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * notification-service's own copy of delivery-service's event contract - not a
 * shared library, see docs/MICROSERVICES.md. Kept as a full mirror (unlike
 * driver-service's trimmed copies) since this service logs every field.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryEvent {

    private DeliveryEventType eventType;

    private Long deliveryId;

    private Long orderId;

    private Long driverId;

    private DeliveryStatus status;

    private LocalDateTime occurredAt;
}
