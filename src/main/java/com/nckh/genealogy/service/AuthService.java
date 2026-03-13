package com.nckh.genealogy.service;

import com.nckh.genealogy.dto.request.auth.LoginRequest;
import com.nckh.genealogy.dto.request.auth.RegisterRequest;
import com.nckh.genealogy.dto.response.auth.AuthResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse register(RegisterRequest request);
}