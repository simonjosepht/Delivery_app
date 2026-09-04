package com.simon.application.event;

import com.simon.application.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * notification-service's own copy of order-service's event contract - not a
 * shared library, see docs/MICROSERVICES.md. Kept as a full mirror (unlike
 * driver-service's trimmed copies) since this service logs every field.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {

    private OrderEventType eventType;

    private Long orderId;

    private Long customerId;

    private OrderStatus status;

    private LocalDateTime occurredAt;
}
