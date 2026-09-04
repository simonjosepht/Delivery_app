package com.simon.application.service.impl;

import com.simon.application.dto.response.DriverResponse;
import com.simon.application.dto.response.DriverSummaryResponse;
import com.simon.application.entity.Driver;
import com.simon.application.enums.DriverStatus;
import com.simon.application.enums.UserRole;
import com.simon.application.event.DeliveryEvent;
import com.simon.application.event.UserEvent;
import com.simon.application.exception.InvalidDriverOperationException;
import com.simon.application.exception.ResourceNotFoundException;
import com.simon.application.mapper.DriverMapper;
import com.simon.application.repository.DriverRepository;
import com.simon.application.service.DriverService;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DriverServiceImpl implements DriverService {

    private final DriverRepository driverRepository;
    private final CacheManager cacheManager;

    public DriverServiceImpl(DriverRepository driverRepository, CacheManager cacheManager) {
        this.driverRepository = driverRepository;
        this.cacheManager = cacheManager;
    }

    @Override
    public List<DriverResponse> getAllDrivers() {
        return driverRepository.findAll().stream()
                .map(DriverMapper::toResponse)
                .toList();
    }

    @Override
    @Cacheable(cacheNames = "availableDrivers")
    public List<DriverResponse> getAvailableDrivers() {
        return driverRepository.findByDriverStatus(DriverStatus.AVAILABLE).stream()
                .map(DriverMapper::toResponse)
                .toList();
    }

    @Override
    public DriverResponse updateDriverStatus(Long driverId, DriverStatus status) {

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + driverId));

        if (status == DriverStatus.ON_DELIVERY) {
            throw new InvalidDriverOperationException("ON_DELIVERY status is managed automatically and cannot be set manually");
        }

        if (driver.getDriverStatus() == DriverStatus.ON_DELIVERY) {
            throw new InvalidDriverOperationException("Cannot change status while a delivery is in progress");
        }

        driver.setDriverStatus(status);

        DriverResponse response = DriverMapper.toResponse(driverRepository.save(driver));
        evictAvailableDrivers();

        return response;
    }

    @Override
    public DriverSummaryResponse getDriverSummary(Long id) {

        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + id));

        return DriverMapper.toSummaryResponse(driver);
    }

    @Override
    public void handleUserRegistered(UserEvent event) {

        if (event.getRole() != UserRole.DRIVER) {
            return;
        }

        if (driverRepository.existsById(event.getUserId())) {
            return;
        }

        Driver driver = Driver.builder()
                .id(event.getUserId())
                .firstName(event.getFirstName())
                .lastName(event.getLastName())
                .email(event.getEmail())
                .driverStatus(DriverStatus.AVAILABLE)
                .build();

        driverRepository.save(driver);
    }

    @Override
    public void handleDeliveryEvent(DeliveryEvent event) {

        DriverStatus newStatus = switch (event.getEventType()) {
            case DELIVERY_ASSIGNED -> DriverStatus.ON_DELIVERY;
            case DELIVERY_COMPLETED -> DriverStatus.AVAILABLE;
            default -> null;
        };

        if (newStatus == null || event.getDriverId() == null) {
            return;
        }

        driverRepository.findById(event.getDriverId()).ifPresent(driver -> {
            driver.setDriverStatus(newStatus);
            driverRepository.save(driver);
            evictAvailableDrivers();
        });
    }

    private void evictAvailableDrivers() {
        Cache cache = cacheManager.getCache("availableDrivers");
        if (cache != null) {
            cache.clear();
        }
    }
}
