package com.nckh.genealogy.dto.response.person;

import com.nckh.genealogy.enums.Gender;

import java.time.LocalDateTime;
import java.util.UUID;

public record PersonResponse(
        UUID id,
        String firstName,
        String lastName,
        String fullName,
        Gender gender,
        String avatarUrl,
        LocalDateTime dateOfBirth,
        LocalDateTime dateOfDeath,
        String citizenIdentificationNumber,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}