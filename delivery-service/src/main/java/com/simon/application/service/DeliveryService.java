package com.simon.application.service;

import com.simon.application.dto.request.AssignDriverRequest;
import com.simon.application.dto.request.CreateDeliveryRequest;
import com.simon.application.dto.response.DeliveryResponse;
import com.simon.application.enums.DeliveryStatus;

import java.util.List;

public interface DeliveryService {

    DeliveryResponse createDelivery(CreateDeliveryRequest request);

    DeliveryResponse assignDriver(Long deliveryId, AssignDriverRequest request);

    DeliveryResponse getDelivery(Long id);

    List<DeliveryResponse> getDeliveriesForDriver(Long driverId);

    List<DeliveryResponse> getAllDeliveries();

    DeliveryResponse updateDeliveryStatus(Long id, DeliveryStatus status);

    DeliveryResponse updateDeliveryStatusAsDriver(Long id, DeliveryStatus status, Long driverId);
}
