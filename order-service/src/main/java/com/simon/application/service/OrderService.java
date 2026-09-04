package com.simon.application.service;

import com.simon.application.dto.request.CreateOrderRequest;
import com.simon.application.dto.response.OrderResponse;
import com.simon.application.dto.response.OrderSummaryResponse;
import com.simon.application.enums.OrderStatus;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(CreateOrderRequest request, Long customerId);

    OrderResponse getOrder(Long id);

    OrderSummaryResponse getOrderSummary(Long id);

    List<OrderResponse> getOrdersForCustomer(Long customerId);

    List<OrderResponse> getAllOrders();

    OrderResponse updateOrderStatus(Long id, OrderStatus status);

    OrderResponse cancelOrder(Long id, Long customerId);
}
