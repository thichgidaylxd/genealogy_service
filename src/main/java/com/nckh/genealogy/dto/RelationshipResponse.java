package com.nckh.genealogy.dto.response.relationship;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RelationshipResponse {

    PersonNode fromPerson;
    PersonNode toPerson;
    String relationshipFromA; // A gọi B là gì
    String relationshipFromB; // B gọi A là gì
    int generationDiff;       // B so với A: dương = B cao hơn, âm = B thấp hơn
    List<PersonNode> path;    // đường đi từ A đến B

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class PersonNode {
        UUID id;
        String fullName;
        String firstName;
        String lastName;
        String avatarUrl;
        Short gender; // 1=MALE, 2=FEMALE
        String relation; // vai trò trong path: "Cha", "Mẹ"...
    }
}