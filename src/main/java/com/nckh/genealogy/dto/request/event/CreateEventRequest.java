package com.nckh.genealogy.dto.request.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateEventRequest(
        @NotBlank(message = "Tên sự kiện không được để trống")
        String name,

        @NotBlank(message = "Mô tả không được để trống")
        String description,

        @NotNull(message = "Thời gian bắt đầu không được để trống")
        LocalDateTime startedAt,

        @NotNull(message = "Thời gian kết thúc không được để trống")
        LocalDateTime endedAt
) {}