package com.simon.application.dto.response;

import com.simon.application.enums.UserRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {

    private Long id;

    private String firstName;

    private String lastName;

    private String email;

    private String phoneNumber;

    private UserRole role;

    private LocalDateTime createdAt;
}