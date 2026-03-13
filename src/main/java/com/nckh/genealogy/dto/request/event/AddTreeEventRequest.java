package com.nckh.genealogy.dto.request.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

// Gắn event vào tree với địa chỉ và tên riêng
public record AddTreeEventRequest(
        @NotNull(message = "Địa chỉ không được để trống")
        UUID addressId,

        @NotBlank(message = "Tên không được để trống")
        String name
) {}