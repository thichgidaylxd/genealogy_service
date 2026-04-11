package com.nckh.genealogy.controller;

import com.nckh.genealogy.dto.request.event.AddPersonToEventRequest;
import com.nckh.genealogy.dto.request.event.AddTreeEventRequest;
import com.nckh.genealogy.dto.request.event.CreateEventRequest;
import com.nckh.genealogy.dto.request.event.CreatePersonEventRequest;
import com.nckh.genealogy.dto.response.ApiResponse;
import com.nckh.genealogy.dto.response.event.EventResponse;
import com.nckh.genealogy.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Event API", description = "Quản lý sự kiện trong cây gia phả")
public class EventController {

    private final EventService eventService;

    @Operation(
            summary = "Tạo sự kiện cho tree",
            description = "Tạo một event mới và gắn trực tiếp vào cây gia phả."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(
            @PathVariable UUID treeId,
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody CreateEventWithTreeRequest body) {

        return ResponseEntity.status(201).body(
                ApiResponse.created(
                        eventService.createEvent(treeId, userId, body.event(), body.treeEvent())
                )
        );
    }

    @Operation(
            summary = "Lấy danh sách event của tree",
            description = "Trả về tất cả các sự kiện thuộc một cây gia phả."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<EventResponse>>> getTreeEvents(
            @PathVariable UUID treeId,
            @AuthenticationPrincipal UUID userId) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        eventService.getTreeEvents(treeId, userId)
                )
        );
    }

    @Operation(
            summary = "Lấy chi tiết event",
            description = "Trả về thông tin chi tiết của một event."
    )
    @GetMapping("/{eventId}")
    public ResponseEntity<ApiResponse<EventResponse>> getEvent(
            @PathVariable UUID treeId,
            @PathVariable UUID eventId,
            @AuthenticationPrincipal UUID userId) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        eventService.getEvent(treeId, eventId, userId)
                )
        );
    }

    @Operation(
            summary = "Thêm person vào event",
            description = "Gắn một person vào sự kiện (ví dụ: tham gia lễ cưới, sinh nhật...)."
    )
    @PostMapping("/{eventId}/persons")
    public ResponseEntity<ApiResponse<EventResponse>> addPersonToEvent(
            @PathVariable UUID treeId,
            @PathVariable UUID eventId,
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody AddPersonToEventRequest request) {

        return ResponseEntity.status(201).body(
                ApiResponse.created(
                        eventService.addPersonToEvent(treeId, eventId, userId, request)
                )
        );
    }

    @Operation(
            summary = "Xóa person khỏi event",
            description = "Gỡ một person ra khỏi sự kiện."
    )
    @DeleteMapping("/{eventId}/persons/{personId}")
    public ResponseEntity<ApiResponse<Void>> removePersonFromEvent(
            @PathVariable UUID treeId,
            @PathVariable UUID eventId,
            @PathVariable UUID personId,
            @AuthenticationPrincipal UUID userId) {

        eventService.removePersonFromEvent(treeId, eventId, personId, userId);

        return ResponseEntity.ok(ApiResponse.noContent());
    }

    @PostMapping("/person-event")
    public ResponseEntity<ApiResponse<EventResponse>> createPersonEvent(
            @PathVariable UUID treeId,
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody CreatePersonEventRequest request) {

        return ResponseEntity.status(201).body(
                ApiResponse.created(
                        eventService.createPersonEvent(treeId, userId, request)
                )
        );
    }

    @Operation(
            summary = "Lấy event của person",
            description = "Trả về danh sách sự kiện mà một person đã tham gia."
    )
    @GetMapping("/persons/{personId}")
    public ResponseEntity<ApiResponse<List<EventResponse>>> getPersonEvents(
            @PathVariable UUID treeId,
            @PathVariable UUID personId,
            @AuthenticationPrincipal UUID userId) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        eventService.getPersonEvents(treeId, personId, userId)
                )
        );
    }

    @Operation(
            summary = "Xóa event",
            description = "Xóa một sự kiện khỏi cây gia phả."
    )
    @DeleteMapping("/{eventId}")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(
            @PathVariable UUID treeId,
            @PathVariable UUID eventId,
            @AuthenticationPrincipal UUID userId) {

        eventService.deleteEvent(treeId, eventId, userId);

        return ResponseEntity.ok(ApiResponse.noContent());
    }

    public record CreateEventWithTreeRequest(
            @Valid CreateEventRequest event,
            @Valid AddTreeEventRequest treeEvent
    ) {}
}