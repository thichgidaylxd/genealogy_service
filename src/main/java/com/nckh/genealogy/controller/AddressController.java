package com.nckh.genealogy.controller;

import com.nckh.genealogy.dto.request.address.AddressRequest;
import com.nckh.genealogy.dto.response.ApiResponse;
import com.nckh.genealogy.dto.response.address.AddressResponse;
import com.nckh.genealogy.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    // ==================== Person Address ====================

    /**
     * POST /api/v1/persons/{personId}/addresses
     * Thêm địa chỉ cho person
     */
    @PostMapping("/api/v1/persons/{personId}/addresses")
    public ResponseEntity<ApiResponse<AddressResponse>> addPersonAddress(
            @PathVariable UUID personId,
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.status(201).body(ApiResponse.created(
                addressService.addPersonAddress(personId, userId, request)));
    }

    /**
     * GET /api/v1/persons/{personId}/addresses
     * Lấy danh sách địa chỉ của person
     */
    @GetMapping("/api/v1/persons/{personId}/addresses")
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getPersonAddresses(
            @PathVariable UUID personId) {
        return ResponseEntity.ok(ApiResponse.success(
                addressService.getPersonAddresses(personId)));
    }

    /**
     * DELETE /api/v1/persons/{personId}/addresses/{addressId}
     * Xóa địa chỉ của person
     */
    @DeleteMapping("/api/v1/persons/{personId}/addresses/{addressId}")
    public ResponseEntity<ApiResponse<Void>> removePersonAddress(
            @PathVariable UUID personId,
            @PathVariable UUID addressId,
            @AuthenticationPrincipal UUID userId) {
        addressService.removePersonAddress(personId, addressId, userId);
        return ResponseEntity.ok(ApiResponse.noContent());
    }

    // ==================== Tree Address ====================

    /**
     * POST /api/v1/trees/{treeId}/addresses
     * Thêm địa chỉ cho tree
     */
    @PostMapping("/api/v1/trees/{treeId}/addresses")
    public ResponseEntity<ApiResponse<AddressResponse>> addTreeAddress(
            @PathVariable UUID treeId,
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.status(201).body(ApiResponse.created(
                addressService.addTreeAddress(treeId, userId, request)));
    }

    /**
     * GET /api/v1/trees/{treeId}/addresses
     * Lấy danh sách địa chỉ của tree
     */
    @GetMapping("/api/v1/trees/{treeId}/addresses")
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getTreeAddresses(
            @PathVariable UUID treeId,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(
                addressService.getTreeAddresses(treeId, userId)));
    }

    /**
     * DELETE /api/v1/trees/{treeId}/addresses/{treeAddressId}
     * Xóa địa chỉ của tree
     */
    @DeleteMapping("/api/v1/trees/{treeId}/addresses/{treeAddressId}")
    public ResponseEntity<ApiResponse<Void>> removeTreeAddress(
            @PathVariable UUID treeId,
            @PathVariable UUID treeAddressId,
            @AuthenticationPrincipal UUID userId) {
        addressService.removeTreeAddress(treeId, treeAddressId, userId);
        return ResponseEntity.ok(ApiResponse.noContent());
    }
}