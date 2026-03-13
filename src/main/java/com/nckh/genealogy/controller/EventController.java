package com.nckh.genealogy.controller;

import com.nckh.genealogy.dto.request.event.AddPersonToEventRequest;
import com.nckh.genealogy.dto.request.event.AddTreeEventRequest;
import com.nckh.genealogy.dto.request.event.CreateEventRequest;
import com.nckh.genealogy.dto.response.ApiResponse;
import com.nckh.genealogy.dto.response.event.EventResponse;
import com.nckh.genealogy.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/trees/{treeId}/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    /**
     * POST /api/v1/trees/{treeId}/events
     * Body: { event: {...}, treeEvent: { addressId, name } }
     * Tạo event và gắn vào tree cùng lúc
     */
    @PostMapping
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(
            @PathVariable UUID treeId,
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody CreateEventWithTreeRequest body) {
        return ResponseEntity.status(201).body(ApiResponse.created(
                eventService.createEvent(treeId, userId, body.event(), body.treeEvent())));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EventResponse>>> getTreeEvents(
            @PathVariable UUID treeId,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(
                eventService.getTreeEvents(treeId, userId)));
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<ApiResponse<EventResponse>> getEvent(
            @PathVariable UUID treeId,
            @PathVariable UUID eventId,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(
                eventService.getEvent(treeId, eventId, userId)));
    }

    @PostMapping("/{eventId}/persons")
    public ResponseEntity<ApiResponse<EventResponse>> addPersonToEvent(
            @PathVariable UUID treeId,
            @PathVariable UUID eventId,
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody AddPersonToEventRequest request) {
        return ResponseEntity.status(201).body(ApiResponse.created(
                eventService.addPersonToEvent(treeId, eventId, userId, request)));
    }

    @DeleteMapping("/{eventId}/persons/{personId}")
    public ResponseEntity<ApiResponse<Void>> removePersonFromEvent(
            @PathVariable UUID treeId,
            @PathVariable UUID eventId,
            @PathVariable UUID personId,
            @AuthenticationPrincipal UUID userId) {
        eventService.removePersonFromEvent(treeId, eventId, personId, userId);
        return ResponseEntity.ok(ApiResponse.noContent());
    }

    @GetMapping("/persons/{personId}")
    public ResponseEntity<ApiResponse<List<EventResponse>>> getPersonEvents(
            @PathVariable UUID treeId,
            @PathVariable UUID personId,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(
                eventService.getPersonEvents(treeId, personId, userId)));
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(
            @PathVariable UUID treeId,
            @PathVariable UUID eventId,
            @AuthenticationPrincipal UUID userId) {
        eventService.deleteEvent(treeId, eventId, userId);
        return ResponseEntity.ok(ApiResponse.noContent());
    }

    // Inner record để nhận body tạo event
    public record CreateEventWithTreeRequest(
            @Valid CreateEventRequest event,
            @Valid AddTreeEventRequest treeEvent
    ) {}
}