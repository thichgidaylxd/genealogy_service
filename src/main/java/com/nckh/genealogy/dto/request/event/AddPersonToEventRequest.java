package com.nckh.genealogy.dto.request.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddPersonToEventRequest(
        @NotNull(message = "Person không được để trống")
        UUID personId,

        @NotNull(message = "Loại sự kiện không được để trống")
        UUID eventTypeId,

        UUID addressId,

        @NotBlank(message = "Tên không được để trống")
        String name
) {}