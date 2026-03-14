package com.nckh.genealogy.controller;

import com.nckh.genealogy.dto.response.ApiResponse;
import com.nckh.genealogy.dto.response.lookup.LookupResponse;
import com.nckh.genealogy.service.LookupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lookup")
@RequiredArgsConstructor
@Tag(name = "Lookup API", description = "API lấy dữ liệu lookup dùng cho dropdown và enum hệ thống")
public class LookupController {

    private final LookupService lookupService;

    @Operation(
            summary = "Lấy danh sách loại địa chỉ",
            description = "Trả về danh sách các loại địa chỉ (ví dụ: HOME, BIRTHPLACE, RESIDENCE...)."
    )
    @GetMapping("/address-types")
    public ResponseEntity<ApiResponse<List<LookupResponse>>> getAddressTypes() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        lookupService.getAddressTypes()
                )
        );
    }

    @Operation(
            summary = "Lấy danh sách loại sự kiện",
            description = "Trả về danh sách các loại sự kiện trong hệ thống (BIRTH, DEATH, MARRIAGE...)."
    )
    @GetMapping("/event-types")
    public ResponseEntity<ApiResponse<List<LookupResponse>>> getEventTypes() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        lookupService.getEventTypes()
                )
        );
    }

    @Operation(
            summary = "Lấy danh sách vai trò trong sự kiện",
            description = "Trả về danh sách role của person trong event (BRIDE, GROOM, CHILD...)."
    )
    @GetMapping("/role-in-events")
    public ResponseEntity<ApiResponse<List<LookupResponse>>> getRoleInEvents() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        lookupService.getRoleInEvents()
                )
        );
    }

    @Operation(
            summary = "Lấy danh sách loại media file",
            description = "Trả về danh sách các loại file media (IMAGE, VIDEO, DOCUMENT...)."
    )
    @GetMapping("/media-file-types")
    public ResponseEntity<ApiResponse<List<LookupResponse>>> getMediaFileTypes() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        lookupService.getMediaFileTypes()
                )
        );
    }
}