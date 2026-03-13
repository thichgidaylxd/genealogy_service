package com.nckh.genealogy.controller;

import com.nckh.genealogy.dto.request.person.CreatePersonRequest;
import com.nckh.genealogy.dto.request.person.UpdatePersonRequest;
import com.nckh.genealogy.dto.response.ApiResponse;
import com.nckh.genealogy.dto.response.PageResponse;
import com.nckh.genealogy.dto.response.person.PersonResponse;
import com.nckh.genealogy.service.PersonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/persons")
@RequiredArgsConstructor
public class PersonController {

    private final PersonService personService;

    /**
     * POST /api/v1/persons
     * Tạo mới nhân vật
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PersonResponse>> createPerson(
            @Valid @RequestBody CreatePersonRequest request) {
        return ResponseEntity.status(201)
                .body(ApiResponse.created(personService.createPerson(request)));
    }

    /**
     * GET /api/v1/persons/{id}
     * Xem chi tiết nhân vật
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PersonResponse>> getPersonById(
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(personService.getPersonById(id)));
    }

    /**
     * PUT /api/v1/persons/{id}
     * Cập nhật nhân vật
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PersonResponse>> updatePerson(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePersonRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Cập nhật thành công", personService.updatePerson(id, request))
        );
    }

    /**
     * DELETE /api/v1/persons/{id}
     * Xóa nhân vật (soft delete)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePerson(
            @PathVariable UUID id) {
        personService.deletePerson(id);
        return ResponseEntity.ok(ApiResponse.noContent());
    }

    /**
     * GET /api/v1/persons?keyword=&page=0&size=10
     * Tìm kiếm nhân vật theo tên, phân trang
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PersonResponse>>> searchPersons(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                ApiResponse.success(personService.searchPersons(keyword, page, size))
        );
    }
}