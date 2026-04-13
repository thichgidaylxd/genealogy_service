package com.nckh.genealogy.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FundResponse {

    UUID id;
    String name;

    // Tree
    UUID treeId;
    String treeName;

    // TreeEvent
    UUID treeEventId;
    String treeEventName;
    LocalDateTime treeEventCreatedAt;

    // Event
    UUID eventId;
    String eventName;
    String eventDescription;
    LocalDateTime eventStartedAt;
    LocalDateTime eventEndedAt;
    Short eventStatus;
}