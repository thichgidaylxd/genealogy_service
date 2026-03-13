package com.nckh.genealogy.controller;

import com.nckh.genealogy.dto.response.ApiResponse;
import com.nckh.genealogy.dto.response.lookup.LookupResponse;
import com.nckh.genealogy.service.LookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/lookup")
@RequiredArgsConstructor
public class LookupController {

    private final LookupService lookupService;

    @GetMapping("/address-types")
    public ResponseEntity<ApiResponse<List<LookupResponse>>> getAddressTypes() {
        return ResponseEntity.ok(ApiResponse.success(lookupService.getAddressTypes()));
    }

    @GetMapping("/event-types")
    public ResponseEntity<ApiResponse<List<LookupResponse>>> getEventTypes() {
        return ResponseEntity.ok(ApiResponse.success(lookupService.getEventTypes()));
    }

    @GetMapping("/role-in-events")
    public ResponseEntity<ApiResponse<List<LookupResponse>>> getRoleInEvents() {
        return ResponseEntity.ok(ApiResponse.success(lookupService.getRoleInEvents()));
    }

    @GetMapping("/media-file-types")
    public ResponseEntity<ApiResponse<List<LookupResponse>>> getMediaFileTypes() {
        return ResponseEntity.ok(ApiResponse.success(lookupService.getMediaFileTypes()));
    }

}