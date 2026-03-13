package com.nckh.genealogy.dto.response.family;

import com.nckh.genealogy.enums.Gender;
import com.nckh.genealogy.enums.UnionType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

// Response cho render cây gia phả toàn bộ
public record TreeGraphResponse(
        List<PersonNode> persons,
        List<FamilyNode> families,
        Meta meta
) {
    // Node đại diện cho 1 person
    public record PersonNode(
            UUID id,
            String firstName,
            String lastName,
            String fullName,
            Gender gender,
            String avatarUrl,
            LocalDateTime dateOfBirth,
            LocalDateTime dateOfDeath,
            int generation        // thế hệ, tính từ gốc = 1
    ) {}

    // Node đại diện cho 1 family (cặp vợ chồng + con)
    public record FamilyNode(
            UUID id,
            UUID parent1Id,
            UUID parent2Id,       // nullable
            UnionType unionType,
            List<UUID> childrenIds
    ) {}

    public record Meta(
            int totalPersons,
            int totalGenerations,
            UUID rootPersonId     // người gốc của cây
    ) {}
}