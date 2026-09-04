package com.simon.application.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * driver-service's own trimmed copy of delivery-service's event contract - only
 * the fields this service actually reacts to (driverId, eventType). Everything
 * else delivery-service's version carries (deliveryId, orderId, status,
 * occurredAt) is silently ignored on deserialization; Spring Kafka's
 * JsonDeserializer does not fail on unrecognized JSON properties.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryEvent {

    private DeliveryEventType eventType;

    private Long driverId;
}
