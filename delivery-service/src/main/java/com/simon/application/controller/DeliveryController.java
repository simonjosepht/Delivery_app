package com.simon.application.controller;

import com.simon.application.dto.request.AssignDriverRequest;
import com.simon.application.dto.request.CreateDeliveryRequest;
import com.simon.application.dto.response.DeliveryResponse;
import com.simon.application.enums.DeliveryStatus;
import com.simon.application.security.UserPrincipal;
import com.simon.application.service.DeliveryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/deliveries")
public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public DeliveryResponse createDelivery(@Valid @RequestBody CreateDeliveryRequest request) {
        return deliveryService.createDelivery(request);
    }

    @PatchMapping("/{id}/assign-driver")
    @PreAuthorize("hasRole('ADMIN')")
    public DeliveryResponse assignDriver(
            @PathVariable Long id,
            @Valid @RequestBody AssignDriverRequest request) {

        return deliveryService.assignDriver(id, request);
    }

    @GetMapping("/{id}")
    @PostAuthorize("hasRole('ADMIN') or returnObject.driverId == authentication.principal.id")
    public DeliveryResponse getDelivery(@PathVariable Long id) {
        return deliveryService.getDelivery(id);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<DeliveryResponse> getAllDeliveries() {
        return deliveryService.getAllDeliveries();
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('DRIVER')")
    public List<DeliveryResponse> getMyDeliveries(Authentication authentication) {
        Long driverId = ((UserPrincipal) authentication.getPrincipal()).getId();
        return deliveryService.getDeliveriesForDriver(driverId);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DRIVER')")
    public DeliveryResponse updateDeliveryStatus(
            @PathVariable Long id,
            @RequestParam DeliveryStatus status,
            Authentication authentication) {

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            return deliveryService.updateDeliveryStatus(id, status);
        }

        Long driverId = ((UserPrincipal) authentication.getPrincipal()).getId();
        return deliveryService.updateDeliveryStatusAsDriver(id, status, driverId);
    }
}
