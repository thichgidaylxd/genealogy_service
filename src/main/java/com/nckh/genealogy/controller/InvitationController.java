package com.nckh.genealogy.controller;

import com.nckh.genealogy.dto.request.invitation.CreateShareLinkRequest;
import com.nckh.genealogy.dto.request.invitation.SendInvitationRequest;
import com.nckh.genealogy.dto.response.ApiResponse;
import com.nckh.genealogy.dto.response.invitation.ShareLinkResponse;
import com.nckh.genealogy.dto.response.tree.TreeResponse;
import com.nckh.genealogy.service.InvitationService;
import com.nckh.genealogy.service.TreeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationService invitationService;
    private final TreeService treeService;
    // ==================== Invitation ====================

    /**
     * POST /api/v1/trees/{treeId}/invitations
     * Gửi lời mời vào tree qua email (ADMIN+)
     */
    @PostMapping("/api/v1/trees/{treeId}/invitations")
    public ResponseEntity<ApiResponse<Void>> sendInvitation(
            @PathVariable UUID treeId,
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody SendInvitationRequest request) {
        invitationService.sendInvitation(treeId, userId, request);
        return ResponseEntity.ok(ApiResponse.noContent());
    }

    /**
     * POST /api/v1/invitations/accept?token=xxx
     * Người được mời click link → chấp nhận lời mời (cần đăng nhập)
     */
    @PostMapping("/api/v1/invitations/accept")
    public ResponseEntity<ApiResponse<Void>> acceptInvitation(
            @RequestParam String token,
            @AuthenticationPrincipal UUID userId) {
        invitationService.acceptInvitation(token, userId);
        return ResponseEntity.ok(ApiResponse.noContent());
    }

    // ==================== Share Link ====================

    /**
     * POST /api/v1/trees/{treeId}/share-links
     * Tạo link chia sẻ gia phả (ADMIN+)
     */
    @PostMapping("/api/v1/trees/{treeId}/share-links")
    public ResponseEntity<ApiResponse<ShareLinkResponse>> createShareLink(
            @PathVariable UUID treeId,
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody CreateShareLinkRequest request) {
        return ResponseEntity.status(201).body(ApiResponse.created(
                invitationService.createShareLink(treeId, userId, request)));
    }

    /**
     * GET /api/v1/trees/{treeId}/share-links
     * Lấy danh sách link đang active (ADMIN+)
     */
    @GetMapping("/api/v1/trees/{treeId}/share-links")
    public ResponseEntity<ApiResponse<List<ShareLinkResponse>>> getActiveShareLinks(
            @PathVariable UUID treeId,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(
                invitationService.getActiveShareLinks(treeId, userId)));
    }

    /**
     * DELETE /api/v1/trees/{treeId}/share-links/{shareLinkId}
     * Thu hồi share link (ADMIN+)
     */
    @DeleteMapping("/api/v1/trees/{treeId}/share-links/{shareLinkId}")
    public ResponseEntity<ApiResponse<Void>> revokeShareLink(
            @PathVariable UUID treeId,
            @PathVariable UUID shareLinkId,
            @AuthenticationPrincipal UUID userId) {
        invitationService.revokeShareLink(treeId, shareLinkId, userId);
        return ResponseEntity.ok(ApiResponse.noContent());
    }

    /**
     * GET /api/v1/share?token=xxx
     * Xem tree qua share link (public, không cần auth)
     */
    /**
     * GET /api/v1/share?token=xxx
     * Xem tree qua share link (public, không cần auth)
     */
    @GetMapping("/api/v1/share")
    public ResponseEntity<ApiResponse<TreeResponse>> getTreeByShareToken(
            @RequestParam String token) {
        UUID treeId = invitationService.getTreeIdByShareToken(token);
        return ResponseEntity.ok(ApiResponse.success(
                treeService.getTreePublic(treeId)));
    }
}