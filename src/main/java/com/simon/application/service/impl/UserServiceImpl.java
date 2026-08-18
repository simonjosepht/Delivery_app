package com.simon.application.service.impl;

import com.simon.application.dto.request.CreateUserRequest;
import com.simon.application.dto.response.UserResponse;
import com.simon.application.entity.User;
import com.simon.application.exception.UserAlreadyExistsException;
import com.simon.application.mapper.UserMapper;
import com.simon.application.repository.UserRepository;
import com.simon.application.service.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserResponse createUser(CreateUserRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists");
        }

        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new UserAlreadyExistsException("Phone number already exists");
        }

        User user = UserMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User savedUser = userRepository.save(user);

        return UserMapper.toResponse(savedUser);
    }

    @Override
    public User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User updateUser(Long id, User user) {

        User existingUser = getUser(id);

        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        existingUser.setPhoneNumber(user.getPhoneNumber());

        return userRepository.save(existingUser);
    }

    @Override
    public void deleteUser(Long id) {

        User user = getUser(id);

        userRepository.delete(user);
    }
}
