package com.simon.application.event;

import com.simon.application.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {

    private EventType eventType;

    private Long orderId;

    private Long customerId;

    private OrderStatus status;

    private LocalDateTime occurredAt;
}
