package com.simon.application.event;

import com.simon.application.service.DriverService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class UserEventListener {

    private final DriverService driverService;

    public UserEventListener(DriverService driverService) {
        this.driverService = driverService;
    }

    @KafkaListener(topics = "user-events", groupId = "driver-service")
    public void onUserEvent(UserEvent event) {
        driverService.handleUserRegistered(event);
    }
}
