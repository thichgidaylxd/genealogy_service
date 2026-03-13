package com.nckh.genealogy.dto.request.auth;

import com.nckh.genealogy.enums.Gender;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record RegisterRequest(
        @NotBlank(message = "Tên đăng nhập không được để trống")
        @Size(min = 3, max = 20, message = "Tên đăng nhập từ 3-20 ký tự")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Tên đăng nhập chỉ chứa chữ, số và dấu _")
        String userName,

        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 6, max = 100, message = "Mật khẩu ít nhất 6 ký tự")
        String password,

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không hợp lệ")
        String email,

        @NotBlank(message = "Họ không được để trống")
        @Size(max = 50)
        String firstName,

        @NotBlank(message = "Tên không được để trống")
        @Size(max = 50)
        String lastName,

        @NotBlank(message = "Số điện thoại không được để trống")
        @Pattern(regexp = "^[0-9]{10}$", message = "Số điện thoại phải có 10 chữ số")
        String phoneNumber,

        @NotNull(message = "Giới tính không được để trống")
        Gender gender,

        @NotNull(message = "Ngày sinh không được để trống")
        LocalDate dateOfBirth
) {}