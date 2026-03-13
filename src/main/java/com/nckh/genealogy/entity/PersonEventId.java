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
public class PersonEventId implements Serializable {

    @Column(name = "person_id")
    private UUID personId;

    @Column(name = "event_id")
    private UUID eventId;
}