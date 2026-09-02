package com.simon.application.mapper;

import com.simon.application.dto.request.CreateUserRequest;
import com.simon.application.dto.response.UserResponse;
import com.simon.application.entity.User;

public class UserMapper {

    private UserMapper() {
    }

    public static User toEntity(CreateUserRequest request) {

        return User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .password(request.getPassword())
                .role(request.getRole())
                .build();
    }

    public static UserResponse toResponse(User user) {

        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .driverStatus(user.getDriverStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }
}