package com.nckh.genealogy.dto.request.tree;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTreeRequest(
        @NotBlank(message = "Tên gia phả không được để trống")
        @Size(max = 50, message = "Tên gia phả tối đa 50 ký tự")
        String name,

        String description
) {}