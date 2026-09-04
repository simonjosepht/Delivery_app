package com.simon.application.event;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventPublisher {

    private static final String USER_EVENTS_TOPIC = "user-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public EventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishUserEvent(UserEvent event) {
        kafkaTemplate.send(USER_EVENTS_TOPIC, event.getUserId().toString(), event);
    }
}
