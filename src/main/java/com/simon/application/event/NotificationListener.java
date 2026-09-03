package com.simon.application.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationListener {

    @KafkaListener(topics = "order-events", groupId = "notification-service")
    public void onOrderEvent(OrderEvent event) {
        log.info("Notification [{}]: order {} (customer {}) is now {}",
                event.getEventType(), event.getOrderId(), event.getCustomerId(), event.getStatus());
    }

    @KafkaListener(topics = "delivery-events", groupId = "notification-service")
    public void onDeliveryEvent(DeliveryEvent event) {
        log.info("Notification [{}]: delivery {} (order {}, driver {}) is now {}",
                event.getEventType(), event.getDeliveryId(), event.getOrderId(), event.getDriverId(), event.getStatus());
    }
}
