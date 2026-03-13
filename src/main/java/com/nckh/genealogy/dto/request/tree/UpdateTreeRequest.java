package com.nckh.genealogy.dto.request.tree;

import jakarta.validation.constraints.Size;

public record UpdateTreeRequest(
        @Size(max = 50, message = "Tên gia phả tối đa 50 ký tự")
        String name,

        String description
) {}