package com.nckh.genealogy.controller;

import com.nckh.genealogy.dto.request.auth.LoginRequest;
import com.nckh.genealogy.dto.request.auth.RegisterRequest;
import com.nckh.genealogy.dto.response.ApiResponse;
import com.nckh.genealogy.dto.response.auth.AuthResponse;
import com.nckh.genealogy.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication API", description = "Các API liên quan đến đăng ký, đăng nhập và OAuth2")
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/v1/auth/login
     */
    @Operation(
            summary = "Đăng nhập",
            description = "Đăng nhập bằng username và password. Trả về JWT token nếu thành công."
    )
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse response = authService.login(request);

        return ResponseEntity.ok(
                ApiResponse.success("Đăng nhập thành công", response)
        );
    }

    /**
     * POST /api/v1/auth/register
     */
    @Operation(
            summary = "Đăng ký tài khoản",
            description = "Tạo tài khoản mới bằng username, email và password."
    )

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        AuthResponse response = authService.register(request);

        return ResponseEntity.status(201)
                .body(ApiResponse.created(response));
    }

    /**
     * OAuth2 login
     */
    @Operation(
            summary = "Đăng nhập bằng Google",
            description = "Spring Security sẽ redirect người dùng sang Google OAuth2 để xác thực."
    )
    @GetMapping("/oauth2/authorize/google")
    public void authorizeGoogle() {
        // Endpoint chỉ dùng để hiển thị trên Swagger
    }

    @Operation(
            summary = "OAuth2 callback Google",
            description = "Sau khi đăng nhập Google thành công, hệ thống sẽ redirect về frontend kèm JWT token."
    )
    @GetMapping("/oauth2/callback/google")
    public void googleCallback() {
        // Endpoint chỉ để document
    }
}