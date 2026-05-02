package com.sbadss.service;

import com.sbadss.dto.AuthResponse;
import com.sbadss.dto.LoginRequest;
import com.sbadss.dto.RegisterRequest;

/*
 * Service for handling authentication and user registration.
 */
public interface AuthService {
    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
