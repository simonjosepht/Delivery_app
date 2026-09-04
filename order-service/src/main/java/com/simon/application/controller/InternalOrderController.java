package com.simon.application.controller;

import com.simon.application.dto.response.OrderSummaryResponse;
import com.simon.application.service.OrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/orders")
public class InternalOrderController {

    private final OrderService orderService;

    public InternalOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/{id}")
    public OrderSummaryResponse getOrderSummary(@PathVariable Long id) {
        return orderService.getOrderSummary(id);
    }
}
