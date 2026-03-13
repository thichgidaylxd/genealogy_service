package com.nckh.genealogy.dto.response.family;

import com.nckh.genealogy.dto.response.person.PersonResponse;
import com.nckh.genealogy.enums.UnionType;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

// Response cho 1 family
public record FamilyResponse(
        UUID id,
        PersonResponse parent1,
        PersonResponse parent2,     // nullable
        UnionType unionType,
        LocalDate fromDate,
        LocalDate toDate,
        List<PersonResponse> children
) {}