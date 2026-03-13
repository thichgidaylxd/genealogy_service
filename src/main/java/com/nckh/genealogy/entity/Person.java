package com.nckh.genealogy.entity;

import com.nckh.genealogy.enums.Gender;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "persons")
public class Person extends BaseEntity {

    @Column(name = "citizen_identification_number", length = 20, unique = true)
    private String citizenIdentificationNumber;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "date_of_birth")
    private LocalDateTime dateOfBirth;

    @Column(name = "date_of_death")
    private LocalDateTime dateOfDeath;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "gender", nullable = false)
    private Gender gender;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}