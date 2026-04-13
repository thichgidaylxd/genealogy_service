package com.nckh.genealogy.controller;

import com.nckh.genealogy.dto.request.album.CreateAlbumRequest;
import com.nckh.genealogy.dto.request.album.UpdateAlbumRequest;
import com.nckh.genealogy.dto.response.album.AlbumResponse;
import com.nckh.genealogy.dto.response.ApiResponse;
import com.nckh.genealogy.service.AlbumService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/trees/{treeId}/albums")
@RequiredArgsConstructor
public class AlbumController {

    private final AlbumService albumService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AlbumResponse>>> getAlbums(
            @PathVariable UUID treeId
    ) {
        return ResponseEntity.ok(ApiResponse.success(albumService.findAll(treeId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AlbumResponse>> createAlbum(
            @PathVariable UUID treeId,
            @AuthenticationPrincipal UUID userId,
            @RequestBody CreateAlbumRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(albumService.createAlbum(treeId, userId, request)));
    }

    @PutMapping("/{albumId}")
    public ResponseEntity<ApiResponse<AlbumResponse>> updateAlbum(
            @PathVariable UUID treeId,
            @PathVariable UUID albumId,
            @RequestBody UpdateAlbumRequest request
    ) {
        request.setId(albumId);
        return ResponseEntity.ok(ApiResponse.success(albumService.updateAlbum(request)));
    }

    @DeleteMapping("/{albumId}")
    public ResponseEntity<ApiResponse<AlbumResponse>> deleteAlbum(
            @PathVariable UUID treeId,
            @PathVariable UUID albumId
    ) {
        return ResponseEntity.ok(ApiResponse.success(albumService.deleteAlbum(albumId)));
    }
}