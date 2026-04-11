package com.nckh.genealogy.dto.request.address;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record AddressRequest(
        @NotBlank(message = "Địa chỉ đầy đủ không được để trống")
        String formattedAddress,

        String addressLine,

        String ward,

        String district,

        String city,

        String province,

        String country,

        BigDecimal latitude,

        BigDecimal longitude,

        // Thông tin liên kết
        @NotNull(message = "Loại địa chỉ không được để trống")
        UUID addressTypeId,

        LocalDateTime fromDate,
        LocalDateTime toDate,
        Boolean isPrimary,
        String description
) {}