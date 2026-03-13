package com.nckh.genealogy.controller;

import com.nckh.genealogy.dto.request.user.ChangePasswordRequest;
import com.nckh.genealogy.dto.request.user.UpdateUserRequest;
import com.nckh.genealogy.dto.response.ApiResponse;
import com.nckh.genealogy.dto.response.user.UserResponse;
import com.nckh.genealogy.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * GET /api/v1/users/me
     * Xem thông tin bản thân
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMe(
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(userService.getMe(userId)));
    }

    /**
     * PUT /api/v1/users/me
     * Cập nhật thông tin bản thân
     */
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateMe(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Cập nhật thành công", userService.updateMe(userId, request))
        );
    }

    /**
     * PATCH /api/v1/users/me/password
     * Đổi mật khẩu
     */
    @PatchMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(userId, request);
        return ResponseEntity.ok(ApiResponse.noContent());
    }

    /**
     * GET /api/v1/users/{id}
     * Xem thông tin user bất kỳ (chỉ ADMIN)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserById(id)));
    }
}