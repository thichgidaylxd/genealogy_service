package com.nckh.genealogy.dto.request.address;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record AddressRequest(
        @NotBlank(message = "Địa chỉ đầy đủ không được để trống")
        String formattedAddress,

        @NotBlank(message = "Địa chỉ dòng không được để trống")
        String addressLine,

        @NotBlank(message = "Phường/Xã không được để trống")
        String ward,

        @NotBlank(message = "Quận/Huyện không được để trống")
        String district,

        @NotBlank(message = "Thành phố không được để trống")
        String city,

        @NotBlank(message = "Tỉnh không được để trống")
        String province,

        @NotBlank(message = "Quốc gia không được để trống")
        String country,

        @NotNull(message = "Vĩ độ không được để trống")
        BigDecimal latitude,

        @NotNull(message = "Kinh độ không được để trống")
        BigDecimal longitude,

        @NotBlank(message = "Place ID không được để trống")
        String placeId,

        // Thông tin liên kết
        @NotNull(message = "Loại địa chỉ không được để trống")
        UUID addressTypeId,

        LocalDateTime fromDate,
        LocalDateTime toDate,
        Boolean isPrimary,
        String description
) {}