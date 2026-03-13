package com.nckh.genealogy.dto.response.event;

import com.nckh.genealogy.dto.response.address.AddressResponse;
import com.nckh.genealogy.dto.response.person.PersonResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record EventResponse(
        UUID id,
        String name,
        String description,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        Short status,
        String createdBy,
        List<PersonInEventResponse> participants
) {
    public record PersonInEventResponse(
            UUID id,
            PersonResponse person,
            String eventType,
            String roleInEvent,
            AddressResponse address,
            String name
    ) {}
}