package com.simon.application.service;

import com.simon.application.dto.request.CreateUserRequest;
import com.simon.application.dto.response.UserResponse;
import com.simon.application.entity.User;

import java.util.List;

public interface UserService {

    UserResponse createUser(CreateUserRequest request);

    User getUser(Long id);

    List<User> getAllUsers();

    User updateUser(Long id, User user);

    void deleteUser(Long id);
}