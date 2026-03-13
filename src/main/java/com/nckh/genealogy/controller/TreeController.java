package com.nckh.genealogy.controller;

import com.nckh.genealogy.dto.request.tree.CreateTreeRequest;
import com.nckh.genealogy.dto.request.tree.UpdateTreeRequest;
import com.nckh.genealogy.dto.response.ApiResponse;
import com.nckh.genealogy.dto.response.tree.TreeMemberResponse;
import com.nckh.genealogy.dto.response.tree.TreeResponse;
import com.nckh.genealogy.enums.TreeMemberRole;
import com.nckh.genealogy.service.TreeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/trees")
@RequiredArgsConstructor
public class TreeController {

    private final TreeService treeService;

    /**
     * POST /api/v1/trees
     * Tạo gia phả mới — người tạo tự động là OWNER
     */
    @PostMapping
    public ResponseEntity<ApiResponse<TreeResponse>> createTree(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody CreateTreeRequest request) {
        return ResponseEntity.status(201)
                .body(ApiResponse.created(treeService.createTree(userId, request)));
    }

    /**
     * GET /api/v1/trees/my
     * Lấy danh sách gia phả của tôi
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<TreeResponse>>> getMyTrees(
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(treeService.getMyTrees(userId)));
    }

    /**
     * GET /api/v1/trees/{treeId}
     * Xem chi tiết gia phả
     */
    @GetMapping("/{treeId}")
    public ResponseEntity<ApiResponse<TreeResponse>> getTreeById(
            @PathVariable UUID treeId,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(treeService.getTreeById(treeId, userId)));
    }

    /**
     * PUT /api/v1/trees/{treeId}
     * Cập nhật gia phả — OWNER, ADMIN
     */
    @PutMapping("/{treeId}")
    public ResponseEntity<ApiResponse<TreeResponse>> updateTree(
            @PathVariable UUID treeId,
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody UpdateTreeRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Cập nhật thành công",
                        treeService.updateTree(treeId, userId, request))
        );
    }

    /**
     * DELETE /api/v1/trees/{treeId}
     * Xóa gia phả — chỉ OWNER
     */
    @DeleteMapping("/{treeId}")
    public ResponseEntity<ApiResponse<Void>> deleteTree(
            @PathVariable UUID treeId,
            @AuthenticationPrincipal UUID userId) {
        treeService.deleteTree(treeId, userId);
        return ResponseEntity.ok(ApiResponse.noContent());
    }

    // ==================== Members ====================

    /**
     * GET /api/v1/trees/{treeId}/members
     * Danh sách thành viên gia phả
     */
    @GetMapping("/{treeId}/members")
    public ResponseEntity<ApiResponse<List<TreeMemberResponse>>> getTreeMembers(
            @PathVariable UUID treeId,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(treeService.getTreeMembers(treeId, userId)));
    }

    /**
     * PATCH /api/v1/trees/{treeId}/members/{targetUserId}/role
     * Thay đổi role thành viên — chỉ OWNER
     */
    @PatchMapping("/{treeId}/members/{targetUserId}/role")
    public ResponseEntity<ApiResponse<Void>> updateMemberRole(
            @PathVariable UUID treeId,
            @PathVariable UUID targetUserId,
            @RequestParam TreeMemberRole role,
            @AuthenticationPrincipal UUID userId) {
        treeService.updateMemberRole(treeId, targetUserId, userId, role);
        return ResponseEntity.ok(ApiResponse.noContent());
    }

    /**
     * DELETE /api/v1/trees/{treeId}/members/{targetUserId}
     * Xóa thành viên — OWNER, ADMIN
     */
    @DeleteMapping("/{treeId}/members/{targetUserId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable UUID treeId,
            @PathVariable UUID targetUserId,
            @AuthenticationPrincipal UUID userId) {
        treeService.removeMember(treeId, targetUserId, userId);
        return ResponseEntity.ok(ApiResponse.noContent());
    }

    /**
     * POST /api/v1/trees/{treeId}/leave
     * Rời gia phả
     */
    @PostMapping("/{treeId}/leave")
    public ResponseEntity<ApiResponse<Void>> leaveTree(
            @PathVariable UUID treeId,
            @AuthenticationPrincipal UUID userId) {
        treeService.leaveTree(treeId, userId);
        return ResponseEntity.ok(ApiResponse.noContent());
    }
}