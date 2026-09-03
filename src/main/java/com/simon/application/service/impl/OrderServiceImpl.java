package com.simon.application.service.impl;

import com.simon.application.dto.request.CreateOrderRequest;
import com.simon.application.dto.response.OrderResponse;
import com.simon.application.entity.Order;
import com.simon.application.entity.User;
import com.simon.application.enums.OrderStatus;
import com.simon.application.exception.InvalidOrderStatusException;
import com.simon.application.exception.ResourceNotFoundException;
import com.simon.application.mapper.OrderMapper;
import com.simon.application.repository.OrderRepository;
import com.simon.application.repository.UserRepository;
import com.simon.application.service.OrderService;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(OrderStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(OrderStatus.CREATED, EnumSet.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(OrderStatus.CONFIRMED, EnumSet.of(OrderStatus.ASSIGNED));
        ALLOWED_TRANSITIONS.put(OrderStatus.ASSIGNED, EnumSet.of(OrderStatus.OUT_FOR_DELIVERY));
        ALLOWED_TRANSITIONS.put(OrderStatus.OUT_FOR_DELIVERY, EnumSet.of(OrderStatus.DELIVERED));
        ALLOWED_TRANSITIONS.put(OrderStatus.DELIVERED, EnumSet.noneOf(OrderStatus.class));
        ALLOWED_TRANSITIONS.put(OrderStatus.CANCELLED, EnumSet.noneOf(OrderStatus.class));
    }

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public OrderServiceImpl(OrderRepository orderRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @Override
    public OrderResponse createOrder(CreateOrderRequest request, Long customerId) {

        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + customerId));

        Order order = OrderMapper.toEntity(request, customer);
        order.setStatus(OrderStatus.CREATED);

        return OrderMapper.toResponse(orderRepository.save(order));
    }

    @Override
    @Cacheable(cacheNames = "orders", key = "#id")
    public OrderResponse getOrder(Long id) {
        return OrderMapper.toResponse(findOrderEntityById(id));
    }

    @Override
    public List<OrderResponse> getOrdersForCustomer(Long customerId) {
        return orderRepository.findByCustomerId(customerId).stream()
                .map(OrderMapper::toResponse)
                .toList();
    }

    @Override
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(OrderMapper::toResponse)
                .toList();
    }

    @Override
    @CachePut(cacheNames = "orders", key = "#id")
    public OrderResponse updateOrderStatus(Long id, OrderStatus status) {

        Order order = findOrderEntityById(id);

        validateTransition(order.getStatus(), status);

        order.setStatus(status);

        return OrderMapper.toResponse(orderRepository.save(order));
    }

    @Override
    @CachePut(cacheNames = "orders", key = "#id")
    public OrderResponse cancelOrder(Long id, Long customerId) {

        Order order = findOrderEntityById(id);

        if (!order.getCustomer().getId().equals(customerId)) {
            throw new AccessDeniedException("You do not have permission to cancel this order");
        }

        validateTransition(order.getStatus(), OrderStatus.CANCELLED);

        order.setStatus(OrderStatus.CANCELLED);

        return OrderMapper.toResponse(orderRepository.save(order));
    }

    private void validateTransition(OrderStatus currentStatus, OrderStatus targetStatus) {
        if (!ALLOWED_TRANSITIONS.get(currentStatus).contains(targetStatus)) {
            throw new InvalidOrderStatusException(
                    "Cannot transition order from " + currentStatus + " to " + targetStatus);
        }
    }

    private Order findOrderEntityById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }
}
