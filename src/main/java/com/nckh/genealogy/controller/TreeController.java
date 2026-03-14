package com.nckh.genealogy.controller;

import com.nckh.genealogy.dto.request.tree.CreateTreeRequest;
import com.nckh.genealogy.dto.request.tree.UpdateTreeRequest;
import com.nckh.genealogy.dto.response.ApiResponse;
import com.nckh.genealogy.dto.response.tree.TreeMemberResponse;
import com.nckh.genealogy.dto.response.tree.TreeResponse;
import com.nckh.genealogy.enums.TreeMemberRole;
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
@RequestMapping("/api/v1/trees")
@RequiredArgsConstructor
@Tag(name = "Tree API", description = "Quản lý cây gia phả và thành viên")
public class TreeController {

    private final TreeService treeService;

    @Operation(
            summary = "Tạo tree mới",
            description = "Tạo một cây gia phả mới. Người tạo sẽ tự động trở thành OWNER."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<TreeResponse>> createTree(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody CreateTreeRequest request) {

        return ResponseEntity.status(201)
                .body(ApiResponse.created(treeService.createTree(userId, request)));
    }

    @Operation(
            summary = "Lấy danh sách tree của tôi",
            description = "Trả về tất cả cây gia phả mà user đang tham gia."
    )
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<TreeResponse>>> getMyTrees(
            @AuthenticationPrincipal UUID userId) {

        return ResponseEntity.ok(
                ApiResponse.success(treeService.getMyTrees(userId))
        );
    }

    @Operation(
            summary = "Lấy chi tiết tree",
            description = "Trả về thông tin chi tiết của một cây gia phả."
    )
    @GetMapping("/{treeId}")
    public ResponseEntity<ApiResponse<TreeResponse>> getTreeById(
            @Parameter(description = "ID của tree")
            @PathVariable UUID treeId,
            @AuthenticationPrincipal UUID userId) {

        return ResponseEntity.ok(
                ApiResponse.success(treeService.getTreeById(treeId, userId))
        );
    }

    @Operation(
            summary = "Cập nhật tree",
            description = "OWNER hoặc ADMIN có thể cập nhật thông tin tree."
    )
    @PutMapping("/{treeId}")
    public ResponseEntity<ApiResponse<TreeResponse>> updateTree(
            @PathVariable UUID treeId,
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody UpdateTreeRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Cập nhật thành công",
                        treeService.updateTree(treeId, userId, request)
                )
        );
    }

    @Operation(
            summary = "Xóa tree",
            description = "Chỉ OWNER mới có quyền xóa cây gia phả."
    )
    @DeleteMapping("/{treeId}")
    public ResponseEntity<ApiResponse<Void>> deleteTree(
            @PathVariable UUID treeId,
            @AuthenticationPrincipal UUID userId) {

        treeService.deleteTree(treeId, userId);

        return ResponseEntity.ok(ApiResponse.noContent());
    }

    // ==================== Members ====================

    @Operation(
            summary = "Lấy danh sách thành viên",
            description = "Trả về danh sách tất cả thành viên của tree."
    )
    @GetMapping("/{treeId}/members")
    public ResponseEntity<ApiResponse<List<TreeMemberResponse>>> getTreeMembers(
            @PathVariable UUID treeId,
            @AuthenticationPrincipal UUID userId) {

        return ResponseEntity.ok(
                ApiResponse.success(treeService.getTreeMembers(treeId, userId))
        );
    }

    @Operation(
            summary = "Thay đổi role thành viên",
            description = "OWNER có thể thay đổi role của thành viên trong tree."
    )
    @PatchMapping("/{treeId}/members/{targetUserId}/role")
    public ResponseEntity<ApiResponse<Void>> updateMemberRole(
            @PathVariable UUID treeId,
            @PathVariable UUID targetUserId,
            @Parameter(description = "Role mới của thành viên")
            @RequestParam TreeMemberRole role,
            @AuthenticationPrincipal UUID userId) {

        treeService.updateMemberRole(treeId, targetUserId, userId, role);

        return ResponseEntity.ok(ApiResponse.noContent());
    }

    @Operation(
            summary = "Xóa thành viên khỏi tree",
            description = "OWNER hoặc ADMIN có thể xóa thành viên khỏi tree."
    )
    @DeleteMapping("/{treeId}/members/{targetUserId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable UUID treeId,
            @PathVariable UUID targetUserId,
            @AuthenticationPrincipal UUID userId) {

        treeService.removeMember(treeId, targetUserId, userId);

        return ResponseEntity.ok(ApiResponse.noContent());
    }

    @Operation(
            summary = "Rời khỏi tree",
            description = "User tự rời khỏi cây gia phả."
    )
    @PostMapping("/{treeId}/leave")
    public ResponseEntity<ApiResponse<Void>> leaveTree(
            @PathVariable UUID treeId,
            @AuthenticationPrincipal UUID userId) {

        treeService.leaveTree(treeId, userId);

        return ResponseEntity.ok(ApiResponse.noContent());
    }
}