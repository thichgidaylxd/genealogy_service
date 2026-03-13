package com.nckh.genealogy.service;

import com.nckh.genealogy.dto.request.event.AddPersonToEventRequest;
import com.nckh.genealogy.dto.request.event.AddTreeEventRequest;
import com.nckh.genealogy.dto.request.event.CreateEventRequest;
import com.nckh.genealogy.dto.response.event.EventResponse;

import java.util.List;
import java.util.UUID;

public interface EventService {

    // Tạo event mới và gắn vào tree ngay
    EventResponse createEvent(UUID treeId, UUID requesterId, CreateEventRequest request,
                              AddTreeEventRequest treeEventRequest);

    EventResponse getEvent(UUID treeId, UUID eventId, UUID requesterId);

    List<EventResponse> getTreeEvents(UUID treeId, UUID requesterId);

    List<EventResponse> getPersonEvents(UUID treeId, UUID personId, UUID requesterId);

    // Thêm person tham gia event (kèm event_type, role, address, name)
    EventResponse addPersonToEvent(UUID treeId, UUID eventId, UUID requesterId,
                                   AddPersonToEventRequest request);

    void removePersonFromEvent(UUID treeId, UUID eventId, UUID personId, UUID requesterId);

    void deleteEvent(UUID treeId, UUID eventId, UUID requesterId);
}