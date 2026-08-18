package com.simon.application.service;

import com.simon.application.dto.request.LoginRequest;
import com.simon.application.dto.response.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);
}