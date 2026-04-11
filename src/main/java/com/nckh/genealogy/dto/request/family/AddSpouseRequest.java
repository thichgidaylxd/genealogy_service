package com.nckh.genealogy.dto.request.family;

import com.nckh.genealogy.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

// Thêm vợ/chồng mới cho person
public record AddSpouseRequest(
        // Thông tin người vợ/chồng mới
        @NotBlank(message = "Họ không được để trống")
        String firstName,

        @NotBlank(message = "Tên không được để trống")
        String lastName,

        @NotNull(message = "Giới tính không được để trống")
        Gender gender,

        LocalDateTime dateOfBirth,
        LocalDateTime dateOfDeath,
        String citizenIdentificationNumber,
        String avatarUrl,

        LocalDate fromDate,
        LocalDate toDate
) {}