package com.nckh.genealogy.dto.request.family;

import com.nckh.genealogy.enums.Gender;
import com.nckh.genealogy.enums.UnionType;
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

        // Thông tin quan hệ
        @NotNull(message = "Loại quan hệ không được để trống")
        UnionType unionType,

        LocalDate fromDate,
        LocalDate toDate
) {}