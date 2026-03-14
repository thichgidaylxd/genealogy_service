package com.nckh.genealogy.controller;

import com.nckh.genealogy.dto.request.family.AddChildRequest;
import com.nckh.genealogy.dto.request.family.AddParentRequest;
import com.nckh.genealogy.dto.request.family.AddSpouseRequest;
import com.nckh.genealogy.dto.request.person.CreatePersonRequest;
import com.nckh.genealogy.dto.response.ApiResponse;
import com.nckh.genealogy.dto.response.family.FamilyResponse;
import com.nckh.genealogy.dto.response.family.PersonFamilyResponse;
import com.nckh.genealogy.dto.response.family.TreeGraphResponse;
import com.nckh.genealogy.dto.response.person.PersonResponse;
import com.nckh.genealogy.service.FamilyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/trees/{treeId}")
@RequiredArgsConstructor
@Tag(name = "Family API", description = "Quản lý quan hệ gia đình trong cây gia phả")
public class FamilyController {

    private final FamilyService familyService;

    @Operation(
            summary = "Lấy toàn bộ cây gia phả",
            description = "Trả về toàn bộ graph của tree để frontend render family tree."
    )
    @GetMapping("/graph")
    public ResponseEntity<ApiResponse<TreeGraphResponse>> getTreeGraph(
            @PathVariable UUID treeId,
            @AuthenticationPrincipal UUID userId) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        familyService.getTreeGraph(treeId, userId)
                )
        );
    }

    @Operation(
            summary = "Lấy thông tin gia đình của person",
            description = "Trả về cha mẹ, vợ/chồng và con của một person."
    )
    @GetMapping("/persons/{personId}/family")
    public ResponseEntity<ApiResponse<PersonFamilyResponse>> getPersonFamily(
            @PathVariable UUID treeId,
            @PathVariable UUID personId,
            @AuthenticationPrincipal UUID userId) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        familyService.getPersonFamily(treeId, personId, userId)
                )
        );
    }

    @Operation(
            summary = "Tạo person đầu tiên trong tree",
            description = "Tạo person gốc của cây gia phả."
    )
    @PostMapping("/persons/first")
    public ResponseEntity<ApiResponse<PersonResponse>> createFirstPersonIntoTree(
            @PathVariable UUID treeId,
            @AuthenticationPrincipal UUID userId,
            @RequestBody CreatePersonRequest request) {

        return ResponseEntity.status(201).body(
                ApiResponse.created(
                        familyService.addFirstPersonIntoTree(treeId, userId, request)
                )
        );
    }

    @Operation(
            summary = "Thêm vợ/chồng cho person",
            description = "Tạo spouse mới và gắn vào person."
    )
    @PostMapping("/persons/{personId}/spouse")
    public ResponseEntity<ApiResponse<FamilyResponse>> addSpouse(
            @PathVariable UUID treeId,
            @PathVariable UUID personId,
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody AddSpouseRequest request) {

        return ResponseEntity.status(201).body(
                ApiResponse.created(
                        familyService.addSpouse(treeId, personId, userId, request)
                )
        );
    }

    @Operation(
            summary = "Thêm cha/mẹ cho person",
            description = "Tạo parent mới và liên kết với person."
    )
    @PostMapping("/persons/{personId}/parent")
    public ResponseEntity<ApiResponse<FamilyResponse>> addParent(
            @PathVariable UUID treeId,
            @PathVariable UUID personId,
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody AddParentRequest request) {

        return ResponseEntity.status(201).body(
                ApiResponse.created(
                        familyService.addParent(treeId, personId, userId, request)
                )
        );
    }

    @Operation(
            summary = "Thêm con vào family",
            description = "Tạo child mới và gắn vào family."
    )
    @PostMapping("/families/{familyId}/child")
    public ResponseEntity<ApiResponse<FamilyResponse>> addChild(
            @PathVariable UUID treeId,
            @PathVariable UUID familyId,
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody AddChildRequest request) {

        return ResponseEntity.status(201).body(
                ApiResponse.created(
                        familyService.addChild(treeId, familyId, userId, request)
                )
        );
    }

    @Operation(
            summary = "Xóa child khỏi family",
            description = "Gỡ một person ra khỏi danh sách con của family."
    )
    @DeleteMapping("/families/{familyId}/children/{personId}")
    public ResponseEntity<ApiResponse<Void>> removeChild(
            @PathVariable UUID treeId,
            @PathVariable UUID familyId,
            @PathVariable UUID personId,
            @AuthenticationPrincipal UUID userId) {

        familyService.removeChild(treeId, familyId, personId, userId);

        return ResponseEntity.ok(ApiResponse.noContent());
    }

    @Operation(
            summary = "Xóa family",
            description = "Soft delete family khỏi cây gia phả."
    )
    @DeleteMapping("/families/{familyId}")
    public ResponseEntity<ApiResponse<Void>> deleteFamily(
            @PathVariable UUID treeId,
            @PathVariable UUID familyId,
            @AuthenticationPrincipal UUID userId) {

        familyService.deleteFamily(treeId, familyId, userId);

        return ResponseEntity.ok(ApiResponse.noContent());
    }
}