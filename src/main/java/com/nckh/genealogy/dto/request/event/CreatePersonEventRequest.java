package com.nckh.genealogy.dto.request.event;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreatePersonEventRequest(

        // Event
        @NotNull String name,
        String description,
        @NotNull LocalDateTime startedAt,
        LocalDateTime endedAt,

        // Person
        @NotNull UUID personId,
        @NotNull UUID eventTypeId,

        // Address (optional)
        UUID addressId

) {}