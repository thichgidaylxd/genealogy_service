package com.nckh.genealogy.controller;

import com.nckh.genealogy.dto.response.ApiResponse;
import com.nckh.genealogy.dto.response.FundResponse;
import com.nckh.genealogy.service.FundService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fund")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FundController {

    FundService fundService;

    @GetMapping
    public ApiResponse<List<FundResponse>> findAllByTreeId(
            @RequestParam("treeId") UUID treeId,
            @AuthenticationPrincipal UUID userId
    ) {
        return ApiResponse.success(fundService.findAllByTreeId(treeId, userId));
    }

    @PostMapping
    public ApiResponse<FundResponse> createFund(
            @RequestParam("treeId") UUID treeId,
            @AuthenticationPrincipal UUID userId,
            @RequestParam("eventId") UUID eventId
    ) {
        return ApiResponse.success(fundService.createFund(treeId, userId, eventId));
    }

    @DeleteMapping
    public ApiResponse<String> deleteFund(
            @RequestParam("fundId") UUID fundId,
            @RequestParam("treeId") UUID treeId,
            @AuthenticationPrincipal UUID userId
    ) {
        fundService.deleteFund(fundId, treeId, userId);
        return ApiResponse.success("Xoá thành công");
    }
}