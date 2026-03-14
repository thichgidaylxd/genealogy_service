package com.nckh.genealogy.controller;

import com.nckh.genealogy.dto.request.user.ChangePasswordRequest;
import com.nckh.genealogy.dto.request.user.UpdateUserRequest;
import com.nckh.genealogy.dto.response.ApiResponse;
import com.nckh.genealogy.dto.response.user.UserResponse;
import com.nckh.genealogy.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "User API", description = "Quản lý thông tin người dùng")
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "Lấy thông tin bản thân",
            description = "Trả về thông tin của user đang đăng nhập."
    )
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMe(
            @AuthenticationPrincipal UUID userId) {

        return ResponseEntity.ok(
                ApiResponse.success(userService.getMe(userId))
        );
    }

    @Operation(
            summary = "Cập nhật thông tin cá nhân",
            description = "User cập nhật thông tin profile của mình."
    )
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateMe(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody UpdateUserRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Cập nhật thành công",
                        userService.updateMe(userId, request)
                )
        );
    }

    @Operation(
            summary = "Đổi mật khẩu",
            description = "User thay đổi mật khẩu của mình."
    )
    @PatchMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody ChangePasswordRequest request) {

        userService.changePassword(userId, request);

        return ResponseEntity.ok(ApiResponse.noContent());
    }

    @Operation(
            summary = "Xem thông tin user",
            description = "Chỉ ADMIN mới có quyền xem thông tin của user khác."
    )
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @Parameter(description = "ID của user")
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                ApiResponse.success(userService.getUserById(id))
        );
    }
}