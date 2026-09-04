package com.simon.application.event;

/**
 * Must list every constant delivery-service's producer can send, even the ones
 * this service doesn't act on (PICKED_UP/OUT_FOR_DELIVERY) - enum deserialization
 * fails on an unrecognized constant name, unlike unrecognized JSON properties.
 */
public enum DeliveryEventType {
    DELIVERY_ASSIGNED,
    DELIVERY_PICKED_UP,
    DELIVERY_OUT_FOR_DELIVERY,
    DELIVERY_COMPLETED
}
