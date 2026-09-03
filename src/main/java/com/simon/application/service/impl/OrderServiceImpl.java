package com.simon.application.service.impl;

import com.simon.application.dto.request.CreateOrderRequest;
import com.simon.application.dto.response.OrderResponse;
import com.simon.application.entity.Order;
import com.simon.application.entity.User;
import com.simon.application.enums.OrderStatus;
import com.simon.application.event.EventPublisher;
import com.simon.application.event.EventType;
import com.simon.application.event.OrderEvent;
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

import java.time.LocalDateTime;
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
    private final EventPublisher eventPublisher;

    public OrderServiceImpl(OrderRepository orderRepository, UserRepository userRepository, EventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public OrderResponse createOrder(CreateOrderRequest request, Long customerId) {

        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + customerId));

        Order order = OrderMapper.toEntity(request, customer);
        order.setStatus(OrderStatus.CREATED);

        Order savedOrder = orderRepository.save(order);

        eventPublisher.publishOrderEvent(OrderEvent.builder()
                .eventType(EventType.ORDER_CREATED)
                .orderId(savedOrder.getId())
                .customerId(customerId)
                .status(savedOrder.getStatus())
                .occurredAt(LocalDateTime.now())
                .build());

        return OrderMapper.toResponse(savedOrder);
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

        Order savedOrder = orderRepository.save(order);

        publishOrderStatusEvent(savedOrder);

        return OrderMapper.toResponse(savedOrder);
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

        Order savedOrder = orderRepository.save(order);

        publishOrderStatusEvent(savedOrder);

        return OrderMapper.toResponse(savedOrder);
    }

    private void validateTransition(OrderStatus currentStatus, OrderStatus targetStatus) {
        if (!ALLOWED_TRANSITIONS.get(currentStatus).contains(targetStatus)) {
            throw new InvalidOrderStatusException(
                    "Cannot transition order from " + currentStatus + " to " + targetStatus);
        }
    }

    private void publishOrderStatusEvent(Order order) {

        EventType eventType = switch (order.getStatus()) {
            case CONFIRMED -> EventType.ORDER_CONFIRMED;
            case CANCELLED -> EventType.ORDER_CANCELLED;
            default -> null;
        };

        if (eventType == null) {
            return;
        }

        eventPublisher.publishOrderEvent(OrderEvent.builder()
                .eventType(eventType)
                .orderId(order.getId())
                .customerId(order.getCustomer().getId())
                .status(order.getStatus())
                .occurredAt(LocalDateTime.now())
                .build());
    }

    private Order findOrderEntityById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }
}
