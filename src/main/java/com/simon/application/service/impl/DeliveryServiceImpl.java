package com.simon.application.service.impl;

import com.simon.application.dto.request.AssignDriverRequest;
import com.simon.application.dto.request.CreateDeliveryRequest;
import com.simon.application.dto.response.DeliveryResponse;
import com.simon.application.entity.Delivery;
import com.simon.application.entity.Order;
import com.simon.application.entity.User;
import com.simon.application.enums.DeliveryStatus;
import com.simon.application.enums.UserRole;
import com.simon.application.exception.InvalidDeliveryOperationException;
import com.simon.application.exception.ResourceNotFoundException;
import com.simon.application.mapper.DeliveryMapper;
import com.simon.application.repository.DeliveryRepository;
import com.simon.application.repository.OrderRepository;
import com.simon.application.repository.UserRepository;
import com.simon.application.service.DeliveryService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

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

    public DeliveryServiceImpl(DeliveryRepository deliveryRepository,
                                OrderRepository orderRepository,
                                UserRepository userRepository) {
        this.deliveryRepository = deliveryRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
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

        delivery.setDriver(driver);
        delivery.setStatus(DeliveryStatus.ASSIGNED);

        return DeliveryMapper.toResponse(deliveryRepository.save(delivery));
    }

    @Override
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
    public DeliveryResponse updateDeliveryStatus(Long id, DeliveryStatus status) {

        Delivery delivery = findDeliveryEntityById(id);

        validateTransition(delivery.getStatus(), status);

        delivery.setStatus(status);

        return DeliveryMapper.toResponse(deliveryRepository.save(delivery));
    }

    @Override
    public DeliveryResponse updateDeliveryStatusAsDriver(Long id, DeliveryStatus status, Long driverId) {

        Delivery delivery = findDeliveryEntityById(id);

        if (delivery.getDriver() == null || !delivery.getDriver().getId().equals(driverId)) {
            throw new AccessDeniedException("You do not have permission to update this delivery");
        }

        validateTransition(delivery.getStatus(), status);

        delivery.setStatus(status);

        return DeliveryMapper.toResponse(deliveryRepository.save(delivery));
    }

    private void validateTransition(DeliveryStatus currentStatus, DeliveryStatus targetStatus) {
        if (!ALLOWED_TRANSITIONS.get(currentStatus).contains(targetStatus)) {
            throw new InvalidDeliveryOperationException(
                    "Cannot transition delivery from " + currentStatus + " to " + targetStatus);
        }
    }

    private Delivery findDeliveryEntityById(Long id) {
        return deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found with id: " + id));
    }
}
