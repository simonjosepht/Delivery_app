package com.simon.application.mapper;

import com.simon.application.dto.response.DriverResponse;
import com.simon.application.dto.response.DriverSummaryResponse;
import com.simon.application.entity.Driver;

public class DriverMapper {

    private DriverMapper() {
    }

    public static DriverResponse toResponse(Driver driver) {

        return DriverResponse.builder()
                .id(driver.getId())
                .firstName(driver.getFirstName())
                .lastName(driver.getLastName())
                .email(driver.getEmail())
                .driverStatus(driver.getDriverStatus())
                .createdAt(driver.getCreatedAt())
                .build();
    }

    public static DriverSummaryResponse toSummaryResponse(Driver driver) {

        return DriverSummaryResponse.builder()
                .id(driver.getId())
                .driverStatus(driver.getDriverStatus())
                .build();
    }
}
