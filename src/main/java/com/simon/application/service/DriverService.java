package com.simon.application.service;

import com.simon.application.dto.response.UserResponse;
import com.simon.application.enums.DriverStatus;

import java.util.List;

public interface DriverService {

    List<UserResponse> getAllDrivers();

    List<UserResponse> getAvailableDrivers();

    UserResponse updateDriverStatus(Long driverId, DriverStatus status);
}
