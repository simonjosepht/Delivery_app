package com.simon.application.event;

import com.simon.application.service.DriverService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class DeliveryEventListener {

    private final DriverService driverService;

    public DeliveryEventListener(DriverService driverService) {
        this.driverService = driverService;
    }

    @KafkaListener(topics = "delivery-events", groupId = "driver-service")
    public void onDeliveryEvent(DeliveryEvent event) {
        driverService.handleDeliveryEvent(event);
    }
}
