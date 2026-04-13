package com.nckh.genealogy.controller;

import com.nckh.genealogy.dto.response.ApiResponse;
import com.nckh.genealogy.service.relationship.RelationshipService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RelationController {
    RelationshipService relationshipService;

    @GetMapping("/api/v1/trees/{treeId}/relationship")
    public ApiResponse<com.nckh.genealogy.dto.response.relationship.RelationshipResponse> findRelationships(
            @PathVariable("treeId") UUID treeId,
            @RequestParam("personAId") UUID personAId,
            @RequestParam("personBId") UUID personBId
    ) {
        return ApiResponse.success(
                relationshipService.getRelationship(treeId, personAId, personBId)
        );
    }
}
