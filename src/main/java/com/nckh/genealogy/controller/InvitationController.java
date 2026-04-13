package com.nckh.genealogy.controller;

import com.nckh.genealogy.dto.request.invitation.CreateShareLinkRequest;
import com.nckh.genealogy.dto.request.invitation.SendInvitationRequest;
import com.nckh.genealogy.dto.response.ApiResponse;
import com.nckh.genealogy.dto.response.family.TreeGraphResponse;
import com.nckh.genealogy.dto.response.invitation.ShareLinkResponse;
import com.nckh.genealogy.dto.response.tree.TreeResponse;
import com.nckh.genealogy.service.FamilyService;
import com.nckh.genealogy.service.InvitationService;
import com.nckh.genealogy.service.TreeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Invitation API", description = "Quản lý lời mời và share link của cây gia phả")
public class InvitationController {

    private final InvitationService invitationService;
    private final TreeService treeService;
    private final FamilyService familyService;
    @Operation(
            summary = "Gửi lời mời vào tree",
            description = "Admin gửi lời mời cho người khác tham gia tree qua email."
    )
    @PostMapping("/api/v1/trees/{treeId}/invitations")
    public ResponseEntity<ApiResponse<Void>> sendInvitation(
            @PathVariable UUID treeId,
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody SendInvitationRequest request) {

        invitationService.sendInvitation(treeId, userId, request);
        return ResponseEntity.ok(ApiResponse.noContent());
    }

    @Operation(
            summary = "Chấp nhận lời mời",
            description = "Người dùng chấp nhận lời mời vào tree bằng token."
    )
    @PostMapping("/api/v1/invitations/accept")
    public ResponseEntity<ApiResponse<Void>> acceptInvitation(
            @Parameter(description = "Invitation token", example = "abc123")
            @RequestParam String token) {

        invitationService.acceptInvitation(token);
        return ResponseEntity.ok(ApiResponse.noContent());
    }

    @Operation(
            summary = "Tạo share link",
            description = "Admin tạo link chia sẻ để người khác xem hoặc tham gia tree."
    )
    @PostMapping("/api/v1/trees/{treeId}/share-links")
    public ResponseEntity<ApiResponse<ShareLinkResponse>> createShareLink(
            @PathVariable UUID treeId,
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody CreateShareLinkRequest request) {

        return ResponseEntity.status(201).body(
                ApiResponse.created(
                        invitationService.createShareLink(treeId, userId, request)
                )
        );
    }

    @Operation(
            summary = "Lấy danh sách share link",
            description = "Trả về danh sách tất cả share link đang active của tree."
    )
    @GetMapping("/api/v1/trees/{treeId}/share-links")
    public ResponseEntity<ApiResponse<List<ShareLinkResponse>>> getActiveShareLinks(
            @PathVariable UUID treeId,
            @AuthenticationPrincipal UUID userId) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        invitationService.getActiveShareLinks(treeId, userId)
                )
        );
    }

    @Operation(
            summary = "Thu hồi share link",
            description = "Admin thu hồi một share link đã tạo."
    )
    @DeleteMapping("/api/v1/trees/{treeId}/share-links/{shareLinkId}")
    public ResponseEntity<ApiResponse<Void>> revokeShareLink(
            @PathVariable UUID treeId,
            @PathVariable UUID shareLinkId,
            @AuthenticationPrincipal UUID userId) {

        invitationService.revokeShareLink(treeId, shareLinkId, userId);
        return ResponseEntity.ok(ApiResponse.noContent());
    }

    @Operation(
            summary = "Xem tree bằng share token",
            description = "Public API cho phép xem tree khi có share token."
    )
    @GetMapping("/api/v1/share")
    public ResponseEntity<ApiResponse<TreeResponse>> getTreeByShareToken(
            @Parameter(description = "Share token", example = "xyz123")
            @RequestParam String token) {

        UUID treeId = invitationService.getTreeIdByShareToken(token);

        return ResponseEntity.ok(
                ApiResponse.success(
                        treeService.getTreePublic(treeId)
                )
        );
    }

    @GetMapping("/api/v1/share/graph")
    public ResponseEntity<ApiResponse<TreeGraphResponse>> getTreeGraphByShareToken(
            @RequestParam String token) {
        UUID treeId = invitationService.getTreeIdByShareToken(token);
        return ResponseEntity.ok(ApiResponse.success(
                familyService.getTreeGraphPublic(treeId)));
    }
}