package com.simon.application.event;

import com.simon.application.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * driver-service's own copy of the event contract user-service produces - not a
 * shared library, see docs/MICROSERVICES.md. The outer class name/package must
 * match the producer's exactly (Kafka's __TypeId__ header resolves to this exact
 * class), but field shape only needs to cover what this service actually uses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEvent {

    private UserEventType eventType;

    private Long userId;

    private String firstName;

    private String lastName;

    private String email;

    private UserRole role;

    private LocalDateTime occurredAt;
}
