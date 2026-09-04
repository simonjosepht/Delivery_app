package com.simon.application.controller;

import com.simon.application.dto.request.CreateOrderRequest;
import com.simon.application.dto.response.OrderResponse;
import com.simon.application.enums.OrderStatus;
import com.simon.application.security.UserPrincipal;
import com.simon.application.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CUSTOMER')")
    public OrderResponse createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            Authentication authentication) {

        Long customerId = ((UserPrincipal) authentication.getPrincipal()).getId();
        return orderService.createOrder(request, customerId);
    }

    @GetMapping("/{id}")
    @PostAuthorize("hasRole('ADMIN') or returnObject.customerId == authentication.principal.id")
    public OrderResponse getOrder(@PathVariable Long id) {
        return orderService.getOrder(id);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<OrderResponse> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public List<OrderResponse> getMyOrders(Authentication authentication) {
        Long customerId = ((UserPrincipal) authentication.getPrincipal()).getId();
        return orderService.getOrdersForCustomer(customerId);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public OrderResponse updateOrderStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status) {

        return orderService.updateOrderStatus(id, status);
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    public OrderResponse cancelOrder(@PathVariable Long id, Authentication authentication) {
        Long customerId = ((UserPrincipal) authentication.getPrincipal()).getId();
        return orderService.cancelOrder(id, customerId);
    }
}
