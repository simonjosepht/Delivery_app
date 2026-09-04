package com.simon.application.service.impl;

import com.simon.application.dto.request.CreateUserRequest;
import com.simon.application.dto.request.UpdateUserRequest;
import com.simon.application.dto.response.UserResponse;
import com.simon.application.dto.response.UserSummaryResponse;
import com.simon.application.entity.User;
import com.simon.application.enums.UserRole;
import com.simon.application.event.EventPublisher;
import com.simon.application.event.EventType;
import com.simon.application.event.UserEvent;
import com.simon.application.exception.ResourceNotFoundException;
import com.simon.application.exception.UnauthorizedRoleAssignmentException;
import com.simon.application.exception.UserAlreadyExistsException;
import com.simon.application.mapper.UserMapper;
import com.simon.application.repository.UserRepository;
import com.simon.application.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EventPublisher eventPublisher;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, EventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public UserResponse createUser(CreateUserRequest request) {

        if (request.getRole() != UserRole.CUSTOMER && request.getRole() != UserRole.DRIVER) {
            throw new UnauthorizedRoleAssignmentException(
                    "Self-registration is only allowed for CUSTOMER or DRIVER roles");
        }

        return createUserInternal(request);
    }

    @Override
    public UserResponse createUserByAdmin(CreateUserRequest request) {
        return createUserInternal(request);
    }

    private UserResponse createUserInternal(CreateUserRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists");
        }

        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new UserAlreadyExistsException("Phone number already exists");
        }

        User user = UserMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User savedUser = userRepository.save(user);

        eventPublisher.publishUserEvent(UserEvent.builder()
                .eventType(EventType.USER_REGISTERED)
                .userId(savedUser.getId())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .occurredAt(LocalDateTime.now())
                .build());

        return UserMapper.toResponse(savedUser);
    }

    @Override
    public UserResponse getUser(Long id) {
        return UserMapper.toResponse(findUserEntityById(id));
    }

    @Override
    public UserSummaryResponse getUserSummary(Long id) {
        return UserMapper.toSummaryResponse(findUserEntityById(id));
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::toResponse)
                .toList();
    }

    @Override
    public UserResponse updateUser(Long id, UpdateUserRequest request) {

        User existingUser = findUserEntityById(id);

        existingUser.setFirstName(request.getFirstName());
        existingUser.setLastName(request.getLastName());
        existingUser.setPhoneNumber(request.getPhoneNumber());

        return UserMapper.toResponse(userRepository.save(existingUser));
    }

    @Override
    public void deleteUser(Long id) {

        User user = findUserEntityById(id);

        userRepository.delete(user);
    }

    private User findUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }
}
