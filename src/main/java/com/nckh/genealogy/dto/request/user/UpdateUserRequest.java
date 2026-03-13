package com.nckh.genealogy.dto.request.user;

import com.nckh.genealogy.enums.Gender;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record UpdateUserRequest(
        @Size(max = 50, message = "Họ tối đa 50 ký tự")
        String firstName,

        @Size(max = 50, message = "Tên tối đa 50 ký tự")
        String lastName,

        @Pattern(regexp = "^[0-9]{10}$", message = "Số điện thoại phải có 10 chữ số")
        String phoneNumber,

        Gender gender,

        LocalDate dateOfBirth,

        String avatarUrl
) {}