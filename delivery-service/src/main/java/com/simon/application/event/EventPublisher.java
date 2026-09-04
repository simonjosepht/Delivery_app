package com.simon.application.event;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventPublisher {

    private static final String DELIVERY_EVENTS_TOPIC = "delivery-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public EventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishDeliveryEvent(DeliveryEvent event) {
        kafkaTemplate.send(DELIVERY_EVENTS_TOPIC, event.getDeliveryId().toString(), event);
    }
}
