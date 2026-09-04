package com.simon.application.service.impl;

import com.simon.application.client.DriverServiceClient;
import com.simon.application.client.DriverSummaryResponse;
import com.simon.application.client.OrderServiceClient;
import com.simon.application.dto.request.AssignDriverRequest;
import com.simon.application.dto.request.CreateDeliveryRequest;
import com.simon.application.dto.response.DeliveryResponse;
import com.simon.application.entity.Delivery;
import com.simon.application.enums.DeliveryStatus;
import com.simon.application.enums.DriverStatus;
import com.simon.application.event.EventPublisher;
import com.simon.application.event.EventType;
import com.simon.application.event.DeliveryEvent;
import com.simon.application.exception.InvalidDeliveryOperationException;
import com.simon.application.exception.ResourceNotFoundException;
import com.simon.application.mapper.DeliveryMapper;
import com.simon.application.repository.DeliveryRepository;
import com.simon.application.service.DeliveryService;
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
    private final OrderServiceClient orderServiceClient;
    private final DriverServiceClient driverServiceClient;
    private final EventPublisher eventPublisher;

    public DeliveryServiceImpl(DeliveryRepository deliveryRepository,
                                OrderServiceClient orderServiceClient,
                                DriverServiceClient driverServiceClient,
                                EventPublisher eventPublisher) {
        this.deliveryRepository = deliveryRepository;
        this.orderServiceClient = orderServiceClient;
        this.driverServiceClient = driverServiceClient;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public DeliveryResponse createDelivery(CreateDeliveryRequest request) {

        orderServiceClient.assertOrderExists(request.getOrderId());

        if (deliveryRepository.existsByOrderId(request.getOrderId())) {
            throw new InvalidDeliveryOperationException(
                    "A delivery already exists for order id: " + request.getOrderId());
        }

        Delivery delivery = DeliveryMapper.toEntity(request.getOrderId());
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

        DriverSummaryResponse driver = driverServiceClient.getDriverSummary(request.getDriverId());

        if (driver.getDriverStatus() != DriverStatus.AVAILABLE) {
            throw new InvalidDeliveryOperationException("Driver is not available for a new delivery");
        }

        delivery.setDriverId(driver.getId());
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

        Delivery savedDelivery = deliveryRepository.save(delivery);

        publishDeliveryStatusEvent(savedDelivery);

        return DeliveryMapper.toResponse(savedDelivery);
    }

    @Override
    @CachePut(cacheNames = "deliveries", key = "#id")
    public DeliveryResponse updateDeliveryStatusAsDriver(Long id, DeliveryStatus status, Long driverId) {

        Delivery delivery = findDeliveryEntityById(id);

        if (delivery.getDriverId() == null || !delivery.getDriverId().equals(driverId)) {
            throw new AccessDeniedException("You do not have permission to update this delivery");
        }

        validateTransition(delivery.getStatus(), status);

        delivery.setStatus(status);

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
                .orderId(delivery.getOrderId())
                .driverId(delivery.getDriverId())
                .status(delivery.getStatus())
                .occurredAt(LocalDateTime.now())
                .build());
    }

    private Delivery findDeliveryEntityById(Long id) {
        return deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found with id: " + id));
    }
}
