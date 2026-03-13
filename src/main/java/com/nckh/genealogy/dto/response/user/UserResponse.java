package com.nckh.genealogy.dto.response.user;

import com.nckh.genealogy.enums.Gender;
import com.nckh.genealogy.enums.UserStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String userName,
        String email,
        String phoneNumber,
        String firstName,
        String lastName,
        String fullName,
        Gender gender,
        String avatarUrl,
        LocalDate dateOfBirth,
        UserStatus status,
        String role,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}