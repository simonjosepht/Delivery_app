package com.simon.application.event;

import com.simon.application.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEvent {

    private EventType eventType;

    private Long userId;

    private String firstName;

    private String lastName;

    private String email;

    private UserRole role;

    private LocalDateTime occurredAt;
}
