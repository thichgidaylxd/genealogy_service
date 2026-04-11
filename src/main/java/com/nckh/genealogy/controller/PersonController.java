package com.nckh.genealogy.controller;

import com.nckh.genealogy.dto.request.person.CreatePersonRequest;
import com.nckh.genealogy.dto.request.person.UpdatePersonRequest;
import com.nckh.genealogy.dto.response.ApiResponse;
import com.nckh.genealogy.dto.response.PageResponse;
import com.nckh.genealogy.dto.response.family.CheckDeletableResponse;
import com.nckh.genealogy.dto.response.person.PersonResponse;
import com.nckh.genealogy.service.FamilyService;
import com.nckh.genealogy.service.PersonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "Person API", description = "Quản lý thông tin nhân vật trong hệ thống gia phả")
public class PersonController {

    private final PersonService personService;
    private final FamilyService familyService;


    @Operation(
            summary = "Tạo nhân vật mới",
            description = "Tạo một person mới trong hệ thống."
    )
    @PostMapping("/api/v1/persons")
    public ResponseEntity<ApiResponse<PersonResponse>> createPerson(
            @Valid @RequestBody CreatePersonRequest request) {

        return ResponseEntity.status(201)
                .body(ApiResponse.created(personService.createPerson(request)));
    }

    @Operation(
            summary = "Lấy thông tin person",
            description = "Trả về thông tin chi tiết của một person."
    )
    @GetMapping("/api/v1/persons/{id}")
    public ResponseEntity<ApiResponse<PersonResponse>> getPersonById(
            @Parameter(description = "ID của person")
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                ApiResponse.success(personService.getPersonById(id))
        );
    }

    @Operation(
            summary = "Cập nhật person",
            description = "Cập nhật thông tin của một person."
    )
    @PutMapping("/api/v1/persons/{id}")
    public ResponseEntity<ApiResponse<PersonResponse>> updatePerson(
            @Parameter(description = "ID của person")
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePersonRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Cập nhật thành công",
                        personService.updatePerson(id, request)
                )
        );
    }

    @Operation(
            summary = "Xóa person",
            description = "Soft delete một person khỏi hệ thống."
    )
    @DeleteMapping("/api/v1/persons/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePerson(
            @Parameter(description = "ID của person")
            @PathVariable UUID id) {

        personService.deletePerson(id);

        return ResponseEntity.ok(ApiResponse.noContent());
    }

    @Operation(
            summary = "Tìm kiếm person",
            description = "Tìm kiếm person theo keyword và hỗ trợ phân trang."
    )
    @GetMapping("/api/v1/persons")
    public ResponseEntity<ApiResponse<PageResponse<PersonResponse>>> searchPersons(
            @Parameter(description = "Từ khóa tìm kiếm theo tên")
            @RequestParam(required = false) String keyword,

            @Parameter(description = "Trang hiện tại", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Số phần tử mỗi trang", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        personService.searchPersons(keyword, page, size)
                )
        );
    }

    @Operation(
            summary = "Kiểm tra có thể xóa person không",
            description = "Kiểm tra xem việc xóa person có phá vỡ cấu trúc cây gia phả không."
    )
    @GetMapping("/api/v1/trees/{treeId}/persons/{personId}/deletable")
    public ResponseEntity<ApiResponse<CheckDeletableResponse>> checkDeletable(
            @PathVariable UUID treeId,
            @PathVariable UUID personId,
            @AuthenticationPrincipal UUID userId) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        familyService.checkDeletable(treeId, personId, userId)
                )
        );
    }

    @Operation(
            summary = "Xóa person (hard delete)",
            description = "Xóa hoàn toàn person và tất cả quan hệ gia đình liên quan."
    )
    @DeleteMapping("/api/v1/trees/{treeId}/persons/{personId}")
    public ResponseEntity<ApiResponse<Void>> hardDeletePerson(
            @PathVariable UUID treeId,
            @PathVariable UUID personId,
            @AuthenticationPrincipal UUID userId) {

        familyService.hardDeletePerson(treeId, personId, userId);
        return ResponseEntity.ok(ApiResponse.noContent());
    }

    @PatchMapping("/api/v1/persons/{personId}/upload-avatar")
    public ResponseEntity<ApiResponse<PersonResponse>> uploadAvatar(
            @PathVariable("personId") UUID personId,
            @RequestPart("file") MultipartFile file
    )
    {
        return ResponseEntity.ok(ApiResponse.success(personService.uploadAvatar(personId, file)));
    }

}