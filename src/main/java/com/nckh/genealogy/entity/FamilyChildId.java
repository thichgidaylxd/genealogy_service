package com.nckh.genealogy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class FamilyChildId implements Serializable {

    @Column(name = "family_id")
    private UUID familyId;

    @Column(name = "person_id")
    private UUID personId;
}