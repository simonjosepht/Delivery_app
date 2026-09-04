package com.simon.application.controller;

import com.simon.application.dto.response.DriverSummaryResponse;
import com.simon.application.service.DriverService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/drivers")
public class InternalDriverController {

    private final DriverService driverService;

    public InternalDriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @GetMapping("/{id}")
    public DriverSummaryResponse getDriverSummary(@PathVariable Long id) {
        return driverService.getDriverSummary(id);
    }
}
