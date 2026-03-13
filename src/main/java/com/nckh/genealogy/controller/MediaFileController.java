package com.nckh.genealogy.controller;

import com.nckh.genealogy.dto.response.ApiResponse;
import com.nckh.genealogy.dto.response.media.MediaFileResponse;
import com.nckh.genealogy.service.MediaFileService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/trees/{treeId}")
@RequiredArgsConstructor
public class MediaFileController {

    private final MediaFileService mediaFileService;

    // ==================== Tree Media ====================

    @PostMapping(value = "/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<MediaFileResponse>> uploadToTree(
            @PathVariable UUID treeId,
            @AuthenticationPrincipal UUID userId,
            @RequestPart("file") MultipartFile file,
            @RequestParam UUID mediaFileTypeId,
            @RequestParam(required = false) String description) {
        return ResponseEntity.status(201).body(ApiResponse.created(
                mediaFileService.uploadToTree(treeId, userId, file, mediaFileTypeId, description)));
    }

    @GetMapping("/media")
    public ResponseEntity<ApiResponse<List<MediaFileResponse>>> getTreeMediaFiles(
            @PathVariable UUID treeId,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(
                mediaFileService.getTreeMediaFiles(treeId, userId)));
    }

    @DeleteMapping("/media/{mediaFileId}")
    public ResponseEntity<ApiResponse<Void>> deleteMediaFile(
            @PathVariable UUID treeId,
            @PathVariable UUID mediaFileId,
            @AuthenticationPrincipal UUID userId) {
        mediaFileService.deleteMediaFile(treeId, mediaFileId, userId);
        return ResponseEntity.ok(ApiResponse.noContent());
    }

    // ==================== Person Media ====================

    @PostMapping(value = "/persons/{personId}/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<MediaFileResponse>> uploadToPerson(
            @PathVariable UUID treeId,
            @PathVariable UUID personId,
            @AuthenticationPrincipal UUID userId,
            @RequestPart("file") MultipartFile file,
            @RequestParam UUID mediaFileTypeId,
            @RequestParam(required = false) String description) {
        return ResponseEntity.status(201).body(ApiResponse.created(
                mediaFileService.uploadToPerson(treeId, personId, userId, file, mediaFileTypeId, description)));
    }

    @GetMapping("/persons/{personId}/media")
    public ResponseEntity<ApiResponse<List<MediaFileResponse>>> getPersonMediaFiles(
            @PathVariable UUID treeId,
            @PathVariable UUID personId,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(
                mediaFileService.getPersonMediaFiles(treeId, personId, userId)));
    }
}