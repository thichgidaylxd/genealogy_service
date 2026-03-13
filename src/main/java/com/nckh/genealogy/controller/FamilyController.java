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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/trees/{treeId}")
@RequiredArgsConstructor
public class FamilyController {

    private final FamilyService familyService;

    /**
     * GET /api/v1/trees/{treeId}/graph
     * Lấy toàn bộ cây gia phả để render
     */
    @GetMapping("/graph")
    public ResponseEntity<ApiResponse<TreeGraphResponse>> getTreeGraph(
            @PathVariable UUID treeId,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(
                familyService.getTreeGraph(treeId, userId)));
    }

    /**
     * GET /api/v1/trees/{treeId}/persons/{personId}/family
     * Xem thông tin gia đình của 1 person
     */
    @GetMapping("/persons/{personId}/family")
    public ResponseEntity<ApiResponse<PersonFamilyResponse>> getPersonFamily(
            @PathVariable UUID treeId,
            @PathVariable UUID personId,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(
                familyService.getPersonFamily(treeId, personId, userId)));
    }

    /**
    *  POST /api/v1/trees/{treeId}/persons/first
    *
    *
    */
    @PostMapping("/persons/first")
    public ResponseEntity<ApiResponse<PersonResponse>> createFirstPersonIntoTree(
            @PathVariable() UUID treeId,
            @AuthenticationPrincipal UUID userId,
            @RequestBody CreatePersonRequest request
    ){
        return ResponseEntity.status(201).body(ApiResponse.created(
                familyService.addFirstPersonIntoTree(treeId, userId, request)));
    }

    /**
     * POST /api/v1/trees/{treeId}/persons/{personId}/spouse
     * Thêm vợ/chồng mới cho person
     */
    @PostMapping("/persons/{personId}/spouse")
    public ResponseEntity<ApiResponse<FamilyResponse>> addSpouse(
            @PathVariable UUID treeId,
            @PathVariable UUID personId,
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody AddSpouseRequest request) {
        return ResponseEntity.status(201).body(ApiResponse.created(
                familyService.addSpouse(treeId, personId, userId, request)));
    }

    /**
     * POST /api/v1/trees/{treeId}/persons/{personId}/parent
     * Thêm cha/mẹ mới cho person
     */
    @PostMapping("/persons/{personId}/parent")
    public ResponseEntity<ApiResponse<FamilyResponse>> addParent(
            @PathVariable UUID treeId,
            @PathVariable UUID personId,
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody AddParentRequest request) {
        return ResponseEntity.status(201).body(ApiResponse.created(
                familyService.addParent(treeId, personId, userId, request)));
    }

    /**
     * POST /api/v1/trees/{treeId}/families/{familyId}/child
     * Thêm con mới vào family
     */
    @PostMapping("/families/{familyId}/child")
    public ResponseEntity<ApiResponse<FamilyResponse>> addChild(
            @PathVariable UUID treeId,
            @PathVariable UUID familyId,
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody AddChildRequest request) {
        return ResponseEntity.status(201).body(ApiResponse.created(
                familyService.addChild(treeId, familyId, userId, request)));
    }

    /**
     * DELETE /api/v1/trees/{treeId}/families/{familyId}/children/{personId}
     * Xóa con khỏi family
     */
    @DeleteMapping("/families/{familyId}/children/{personId}")
    public ResponseEntity<ApiResponse<Void>> removeChild(
            @PathVariable UUID treeId,
            @PathVariable UUID familyId,
            @PathVariable UUID personId,
            @AuthenticationPrincipal UUID userId) {
        familyService.removeChild(treeId, familyId, personId, userId);
        return ResponseEntity.ok(ApiResponse.noContent());
    }

    /**
     * DELETE /api/v1/trees/{treeId}/families/{familyId}
     * Xóa family (soft delete)
     */
    @DeleteMapping("/families/{familyId}")
    public ResponseEntity<ApiResponse<Void>> deleteFamily(
            @PathVariable UUID treeId,
            @PathVariable UUID familyId,
            @AuthenticationPrincipal UUID userId) {
        familyService.deleteFamily(treeId, familyId, userId);
        return ResponseEntity.ok(ApiResponse.noContent());
    }
}