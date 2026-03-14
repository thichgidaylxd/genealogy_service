package com.nckh.genealogy.controller;

import com.nckh.genealogy.dto.request.address.AddressRequest;
import com.nckh.genealogy.dto.request.address.UpdatePersonAddressRequest;
import com.nckh.genealogy.dto.request.address.UpdateTreeAddressRequest;
import com.nckh.genealogy.dto.response.ApiResponse;
import com.nckh.genealogy.dto.response.address.AddressResponse;
import com.nckh.genealogy.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "Address API", description = "Quản lý địa chỉ của Person và Tree")
public class AddressController {

    private final AddressService addressService;

    // ==================== Person Address ====================

    @Operation(
            summary = "Thêm địa chỉ cho Person",
            description = "API dùng để thêm một địa chỉ mới cho person trong hệ thống."
    )
    @PostMapping("/api/v1/trees/{treeId}/persons/{personId}/addresses")
    public ResponseEntity<ApiResponse<AddressResponse>> addPersonAddress(
            @PathVariable UUID treeId,
            @PathVariable UUID personId,
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody AddressRequest request) {

        return ResponseEntity.status(201).body(
                ApiResponse.created(
                        addressService.addPersonAddress(treeId, personId, userId, request)
                )
        );
    }

    @Operation(
            summary = "Lấy danh sách địa chỉ của Person",
            description = "Trả về toàn bộ danh sách địa chỉ đã gắn với person."
    )
    @GetMapping("/api/v1/trees/{treeId}/persons/{personId}/addresses")
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getPersonAddresses(
            @PathVariable UUID treeId,
            @PathVariable UUID personId,
            @AuthenticationPrincipal UUID userId) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        addressService.getPersonAddresses(treeId, personId, userId)
                )
        );
    }

    @Operation(
            summary = "Cập nhật địa chỉ của Person",
            description = "Tạo address mới và cập nhật liên kết, tự động dọn address cũ nếu không còn ai dùng."
    )
    @PutMapping("/api/v1/trees/{treeId}/persons/{personId}/addresses/{addressId}")
    public ResponseEntity<ApiResponse<AddressResponse>> updatePersonAddress(
            @PathVariable UUID treeId,
            @PathVariable UUID personId,
            @PathVariable UUID addressId,
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody UpdatePersonAddressRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        addressService.updatePersonAddress(treeId, personId, userId, addressId, request)
                )
        );
    }

    @Operation(
            summary = "Xóa địa chỉ của Person",
            description = "Xóa một địa chỉ đã gắn với person."
    )
    @DeleteMapping("/api/v1/trees/{treeId}/persons/{personId}/addresses/{addressId}")
    public ResponseEntity<ApiResponse<Void>> removePersonAddress(
            @PathVariable UUID treeId,
            @PathVariable UUID personId,
            @PathVariable UUID addressId,
            @AuthenticationPrincipal UUID userId) {

        addressService.removePersonAddress(treeId, personId, addressId, userId);

        return ResponseEntity.ok(ApiResponse.noContent());
    }

    // ==================== Tree Address ====================

    @Operation(
            summary = "Thêm địa chỉ cho Tree",
            description = "API dùng để thêm địa chỉ mới cho cây gia phả."
    )
    @PostMapping("/api/v1/trees/{treeId}/addresses")
    public ResponseEntity<ApiResponse<AddressResponse>> addTreeAddress(
            @PathVariable UUID treeId,
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody AddressRequest request) {

        return ResponseEntity.status(201).body(
                ApiResponse.created(
                        addressService.addTreeAddress(treeId, userId, request)
                )
        );
    }

    @Operation(
            summary = "Lấy danh sách địa chỉ của Tree",
            description = "Trả về danh sách tất cả địa chỉ của một cây gia phả."
    )
    @GetMapping("/api/v1/trees/{treeId}/addresses")
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getTreeAddresses(
            @PathVariable UUID treeId,
            @AuthenticationPrincipal UUID userId) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        addressService.getTreeAddresses(treeId, userId)
                )
        );
    }

    @Operation(
            summary = "Cập nhật địa chỉ của Tree",
            description = "Tạo address mới và cập nhật liên kết, tự động dọn address cũ nếu không còn ai dùng."
    )
    @PutMapping("/api/v1/trees/{treeId}/addresses/{treeAddressId}")
    public ResponseEntity<ApiResponse<AddressResponse>> updateTreeAddress(
            @PathVariable UUID treeId,
            @PathVariable UUID treeAddressId,
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody UpdateTreeAddressRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        addressService.updateTreeAddress(treeId, treeAddressId, userId, request)
                )
        );
    }

    @Operation(
            summary = "Xóa địa chỉ của Tree",
            description = "Xóa một địa chỉ đã được gắn với cây gia phả."
    )
    @DeleteMapping("/api/v1/trees/{treeId}/addresses/{treeAddressId}")
    public ResponseEntity<ApiResponse<Void>> removeTreeAddress(
            @PathVariable UUID treeId,
            @PathVariable UUID treeAddressId,
            @AuthenticationPrincipal UUID userId) {

        addressService.removeTreeAddress(treeId, treeAddressId, userId);

        return ResponseEntity.ok(ApiResponse.noContent());
    }
}