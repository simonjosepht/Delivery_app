package com.simon.application.controller;

import com.simon.application.dto.request.UpdateDriverStatusRequest;
import com.simon.application.dto.response.UserResponse;
import com.simon.application.security.UserPrincipal;
import com.simon.application.service.DriverService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/drivers")
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAllDrivers() {
        return driverService.getAllDrivers();
    }

    @GetMapping("/available")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAvailableDrivers() {
        return driverService.getAvailableDrivers();
    }

    @PatchMapping("/me/status")
    @PreAuthorize("hasRole('DRIVER')")
    public UserResponse updateMyDriverStatus(
            @Valid @RequestBody UpdateDriverStatusRequest request,
            Authentication authentication) {

        Long driverId = ((UserPrincipal) authentication.getPrincipal()).getId();
        return driverService.updateDriverStatus(driverId, request.getStatus());
    }
}
