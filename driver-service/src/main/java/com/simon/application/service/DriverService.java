package com.simon.application.service;

import com.simon.application.dto.response.DriverResponse;
import com.simon.application.dto.response.DriverSummaryResponse;
import com.simon.application.enums.DriverStatus;
import com.simon.application.event.DeliveryEvent;
import com.simon.application.event.UserEvent;

import java.util.List;

public interface DriverService {

    List<DriverResponse> getAllDrivers();

    List<DriverResponse> getAvailableDrivers();

    DriverResponse updateDriverStatus(Long driverId, DriverStatus status);

    DriverSummaryResponse getDriverSummary(Long id);

    void handleUserRegistered(UserEvent event);

    void handleDeliveryEvent(DeliveryEvent event);
}
