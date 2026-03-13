package com.nckh.genealogy.dto.request.person;

import com.nckh.genealogy.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CreatePersonRequest(
        @NotBlank(message = "Họ không được để trống")
        @Size(max = 100)
        String firstName,

        @NotBlank(message = "Tên không được để trống")
        @Size(max = 100)
        String lastName,

        @NotNull(message = "Giới tính không được để trống")
        Gender gender,

        LocalDateTime dateOfBirth,

        LocalDateTime dateOfDeath,

        @Size(max = 20, message = "Số CCCD tối đa 20 ký tự")
        String citizenIdentificationNumber,

        String avatarUrl
) {}