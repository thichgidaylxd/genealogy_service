package com.nckh.genealogy.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Tên đăng nhập không được để trống")
        String userName,

        @NotBlank(message = "Mật khẩu không được để trống")
        String password
) {}