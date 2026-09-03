package com.simon.application.service;

import com.simon.application.dto.request.CreateUserRequest;
import com.simon.application.dto.request.UpdateUserRequest;
import com.simon.application.dto.response.UserResponse;

import java.util.List;

public interface UserService {

    UserResponse createUser(CreateUserRequest request);

    UserResponse createUserByAdmin(CreateUserRequest request);

    UserResponse getUser(Long id);

    List<UserResponse> getAllUsers();

    UserResponse updateUser(Long id, UpdateUserRequest request);

    void deleteUser(Long id);
}