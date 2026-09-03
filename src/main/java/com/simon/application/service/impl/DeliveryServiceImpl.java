package com.simon.application.service.impl;

import com.simon.application.dto.request.AssignDriverRequest;
import com.simon.application.dto.request.CreateDeliveryRequest;
import com.simon.application.dto.response.DeliveryResponse;
import com.simon.application.entity.Delivery;
import com.simon.application.entity.Order;
import com.simon.application.entity.User;
import com.simon.application.enums.DeliveryStatus;
import com.simon.application.enums.DriverStatus;
import com.simon.application.enums.UserRole;
import com.simon.application.event.DeliveryEvent;
import com.simon.application.event.EventPublisher;
import com.simon.application.event.EventType;
import com.simon.application.exception.InvalidDeliveryOperationException;
import com.simon.application.exception.ResourceNotFoundException;
import com.simon.application.mapper.DeliveryMapper;
import com.simon.application.repository.DeliveryRepository;
import com.simon.application.repository.OrderRepository;
import com.simon.application.repository.UserRepository;
import com.simon.application.service.DeliveryService;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
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
public class DeliveryServiceImpl implements DeliveryService {

    private static final Map<DeliveryStatus, Set<DeliveryStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(DeliveryStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(DeliveryStatus.PENDING, EnumSet.noneOf(DeliveryStatus.class));
        ALLOWED_TRANSITIONS.put(DeliveryStatus.ASSIGNED, EnumSet.of(DeliveryStatus.PICKED_UP));
        ALLOWED_TRANSITIONS.put(DeliveryStatus.PICKED_UP, EnumSet.of(DeliveryStatus.OUT_FOR_DELIVERY));
        ALLOWED_TRANSITIONS.put(DeliveryStatus.OUT_FOR_DELIVERY, EnumSet.of(DeliveryStatus.DELIVERED));
        ALLOWED_TRANSITIONS.put(DeliveryStatus.DELIVERED, EnumSet.noneOf(DeliveryStatus.class));
    }

    private final DeliveryRepository deliveryRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CacheManager cacheManager;
    private final EventPublisher eventPublisher;

    public DeliveryServiceImpl(DeliveryRepository deliveryRepository,
                                OrderRepository orderRepository,
                                UserRepository userRepository,
                                CacheManager cacheManager,
                                EventPublisher eventPublisher) {
        this.deliveryRepository = deliveryRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.cacheManager = cacheManager;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public DeliveryResponse createDelivery(CreateDeliveryRequest request) {

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + request.getOrderId()));

        if (deliveryRepository.existsByOrderId(order.getId())) {
            throw new InvalidDeliveryOperationException(
                    "A delivery already exists for order id: " + order.getId());
        }

        Delivery delivery = DeliveryMapper.toEntity(order);
        delivery.setStatus(DeliveryStatus.PENDING);

        return DeliveryMapper.toResponse(deliveryRepository.save(delivery));
    }

    @Override
    @CachePut(cacheNames = "deliveries", key = "#deliveryId")
    public DeliveryResponse assignDriver(Long deliveryId, AssignDriverRequest request) {

        Delivery delivery = findDeliveryEntityById(deliveryId);

        if (delivery.getStatus() != DeliveryStatus.PENDING) {
            throw new InvalidDeliveryOperationException(
                    "A driver can only be assigned to a delivery in PENDING status");
        }

        User driver = userRepository.findById(request.getDriverId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getDriverId()));

        if (driver.getRole() != UserRole.DRIVER) {
            throw new InvalidDeliveryOperationException("Assigned user must have the DRIVER role");
        }

        if (driver.getDriverStatus() != DriverStatus.AVAILABLE) {
            throw new InvalidDeliveryOperationException("Driver is not available for a new delivery");
        }

        driver.setDriverStatus(DriverStatus.ON_DELIVERY);
        userRepository.save(driver);
        evictAvailableDrivers();

        delivery.setDriver(driver);
        delivery.setStatus(DeliveryStatus.ASSIGNED);

        Delivery savedDelivery = deliveryRepository.save(delivery);

        publishDeliveryStatusEvent(savedDelivery);

        return DeliveryMapper.toResponse(savedDelivery);
    }

    @Override
    @Cacheable(cacheNames = "deliveries", key = "#id")
    public DeliveryResponse getDelivery(Long id) {
        return DeliveryMapper.toResponse(findDeliveryEntityById(id));
    }

    @Override
    public List<DeliveryResponse> getDeliveriesForDriver(Long driverId) {
        return deliveryRepository.findByDriverId(driverId).stream()
                .map(DeliveryMapper::toResponse)
                .toList();
    }

    @Override
    public List<DeliveryResponse> getAllDeliveries() {
        return deliveryRepository.findAll().stream()
                .map(DeliveryMapper::toResponse)
                .toList();
    }

    @Override
    @CachePut(cacheNames = "deliveries", key = "#id")
    public DeliveryResponse updateDeliveryStatus(Long id, DeliveryStatus status) {

        Delivery delivery = findDeliveryEntityById(id);

        validateTransition(delivery.getStatus(), status);

        delivery.setStatus(status);

        releaseDriverIfDelivered(delivery);

        Delivery savedDelivery = deliveryRepository.save(delivery);

        publishDeliveryStatusEvent(savedDelivery);

        return DeliveryMapper.toResponse(savedDelivery);
    }

    @Override
    @CachePut(cacheNames = "deliveries", key = "#id")
    public DeliveryResponse updateDeliveryStatusAsDriver(Long id, DeliveryStatus status, Long driverId) {

        Delivery delivery = findDeliveryEntityById(id);

        if (delivery.getDriver() == null || !delivery.getDriver().getId().equals(driverId)) {
            throw new AccessDeniedException("You do not have permission to update this delivery");
        }

        validateTransition(delivery.getStatus(), status);

        delivery.setStatus(status);

        releaseDriverIfDelivered(delivery);

        Delivery savedDelivery = deliveryRepository.save(delivery);

        publishDeliveryStatusEvent(savedDelivery);

        return DeliveryMapper.toResponse(savedDelivery);
    }

    private void validateTransition(DeliveryStatus currentStatus, DeliveryStatus targetStatus) {
        if (!ALLOWED_TRANSITIONS.get(currentStatus).contains(targetStatus)) {
            throw new InvalidDeliveryOperationException(
                    "Cannot transition delivery from " + currentStatus + " to " + targetStatus);
        }
    }

    private void releaseDriverIfDelivered(Delivery delivery) {
        if (delivery.getStatus() == DeliveryStatus.DELIVERED && delivery.getDriver() != null) {
            User driver = delivery.getDriver();
            driver.setDriverStatus(DriverStatus.AVAILABLE);
            userRepository.save(driver);
            evictAvailableDrivers();
        }
    }

    private void evictAvailableDrivers() {
        Cache cache = cacheManager.getCache("availableDrivers");
        if (cache != null) {
            cache.clear();
        }
    }

    private void publishDeliveryStatusEvent(Delivery delivery) {

        EventType eventType = switch (delivery.getStatus()) {
            case ASSIGNED -> EventType.DELIVERY_ASSIGNED;
            case PICKED_UP -> EventType.DELIVERY_PICKED_UP;
            case OUT_FOR_DELIVERY -> EventType.DELIVERY_OUT_FOR_DELIVERY;
            case DELIVERED -> EventType.DELIVERY_COMPLETED;
            default -> null;
        };

        if (eventType == null) {
            return;
        }

        eventPublisher.publishDeliveryEvent(DeliveryEvent.builder()
                .eventType(eventType)
                .deliveryId(delivery.getId())
                .orderId(delivery.getOrder().getId())
                .driverId(delivery.getDriver() != null ? delivery.getDriver().getId() : null)
                .status(delivery.getStatus())
                .occurredAt(LocalDateTime.now())
                .build());
    }

    private Delivery findDeliveryEntityById(Long id) {
        return deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found with id: " + id));
    }
}
