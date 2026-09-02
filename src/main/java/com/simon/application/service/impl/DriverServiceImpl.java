package com.simon.application.service.impl;

import com.simon.application.dto.response.UserResponse;
import com.simon.application.entity.User;
import com.simon.application.enums.DriverStatus;
import com.simon.application.enums.UserRole;
import com.simon.application.exception.InvalidDriverOperationException;
import com.simon.application.exception.ResourceNotFoundException;
import com.simon.application.mapper.UserMapper;
import com.simon.application.repository.UserRepository;
import com.simon.application.service.DriverService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DriverServiceImpl implements DriverService {

    private final UserRepository userRepository;

    public DriverServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<UserResponse> getAllDrivers() {
        return userRepository.findByRole(UserRole.DRIVER).stream()
                .map(UserMapper::toResponse)
                .toList();
    }

    @Override
    public List<UserResponse> getAvailableDrivers() {
        return userRepository.findByRoleAndDriverStatus(UserRole.DRIVER, DriverStatus.AVAILABLE).stream()
                .map(UserMapper::toResponse)
                .toList();
    }

    @Override
    public UserResponse updateDriverStatus(Long driverId, DriverStatus status) {

        User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + driverId));

        if (driver.getRole() != UserRole.DRIVER) {
            throw new InvalidDriverOperationException("User with id: " + driverId + " is not a driver");
        }

        if (status == DriverStatus.ON_DELIVERY) {
            throw new InvalidDriverOperationException("ON_DELIVERY status is managed automatically and cannot be set manually");
        }

        if (driver.getDriverStatus() == DriverStatus.ON_DELIVERY) {
            throw new InvalidDriverOperationException("Cannot change status while a delivery is in progress");
        }

        driver.setDriverStatus(status);

        return UserMapper.toResponse(userRepository.save(driver));
    }
}
