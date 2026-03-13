package com.nckh.genealogy.dto.request.family;

import com.nckh.genealogy.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

// Thêm con mới vào family
public record AddChildRequest(
        @NotBlank(message = "Họ không được để trống")
        String firstName,

        @NotBlank(message = "Tên không được để trống")
        String lastName,

        @NotNull(message = "Giới tính không được để trống")
        Gender gender,

        LocalDateTime dateOfBirth,
        LocalDateTime dateOfDeath,
        String citizenIdentificationNumber,
        String avatarUrl
) {}