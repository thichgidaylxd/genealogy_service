package com.nckh.genealogy.dto.request.invitation;

import com.nckh.genealogy.enums.SharePermission;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateShareLinkRequest(
        @NotNull(message = "Quyền truy cập không được để trống")
        SharePermission permission,

        // null = không hết hạn
        LocalDateTime expiredAt
) {}