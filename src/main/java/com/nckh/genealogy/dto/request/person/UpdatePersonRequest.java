package com.nckh.genealogy.dto.request.person;

import com.nckh.genealogy.enums.Gender;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record UpdatePersonRequest(
        @Size(max = 100)
        String firstName,

        @Size(max = 100)
        String lastName,

        Gender gender,

        LocalDateTime dateOfBirth,

        LocalDateTime dateOfDeath,

        @Size(max = 20)
        String citizenIdentificationNumber,

        String avatarUrl
) {}