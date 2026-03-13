package com.nckh.genealogy.controller;

import com.nckh.genealogy.dto.request.auth.LoginRequest;
import com.nckh.genealogy.dto.request.auth.RegisterRequest;
import com.nckh.genealogy.dto.response.ApiResponse;
import com.nckh.genealogy.dto.response.auth.AuthResponse;
import com.nckh.genealogy.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/v1/auth/login
     * Đăng nhập bằng username + password
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công", response));
    }

    /**
     * POST /api/v1/auth/register
     * Đăng ký tài khoản mới
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(201).body(ApiResponse.created(response));
    }

    /**
     * GET /api/v1/auth/oauth2/authorize/google
     * Spring Security tự xử lý redirect sang Google
     * Endpoint này chỉ để document, không cần implement
     */

    /**
     * GET /api/v1/auth/oauth2/callback/google
     * Spring Security + OAuth2SuccessHandler tự xử lý
     * Sau khi thành công sẽ redirect về frontend kèm token
     */
}