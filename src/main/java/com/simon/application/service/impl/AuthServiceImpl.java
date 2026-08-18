package com.simon.application.service.impl;

import com.simon.application.dto.request.LoginRequest;
import com.simon.application.dto.response.LoginResponse;
import com.simon.application.entity.User;
import com.simon.application.repository.UserRepository;
import com.simon.application.security.JwtService;
import com.simon.application.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;

    }

    @Override
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("Invalid email or password"));

        boolean passwordMatches = passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        );

        if (!passwordMatches) {
            throw new RuntimeException("Invalid email or password");
        }

        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());

        return LoginResponse.builder()
                .token(token)
                .build();
    }
}