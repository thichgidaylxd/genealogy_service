package com.nckh.genealogy.controller;

import com.nckh.genealogy.dto.request.person.CreatePersonRequest;
import com.nckh.genealogy.dto.request.person.UpdatePersonRequest;
import com.nckh.genealogy.dto.response.ApiResponse;
import com.nckh.genealogy.dto.response.PageResponse;
import com.nckh.genealogy.dto.response.person.PersonResponse;
import com.nckh.genealogy.service.PersonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/persons")
@RequiredArgsConstructor
@Tag(name = "Person API", description = "Quản lý thông tin nhân vật trong hệ thống gia phả")
public class PersonController {

    private final PersonService personService;

    @Operation(
            summary = "Tạo nhân vật mới",
            description = "Tạo một person mới trong hệ thống."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<PersonResponse>> createPerson(
            @Valid @RequestBody CreatePersonRequest request) {

        return ResponseEntity.status(201)
                .body(ApiResponse.created(personService.createPerson(request)));
    }

    @Operation(
            summary = "Lấy thông tin person",
            description = "Trả về thông tin chi tiết của một person."
    )
    @GetMapping("/{id}")
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
    @PutMapping("/{id}")
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
    @DeleteMapping("/{id}")
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
    @GetMapping
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
}