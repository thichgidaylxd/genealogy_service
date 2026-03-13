package com.nckh.genealogy.dto.response.invitation;

import com.nckh.genealogy.enums.SharePermission;

import java.time.LocalDateTime;
import java.util.UUID;

public record ShareLinkResponse(
        UUID id,
        String shareUrl,
        SharePermission permission,
        LocalDateTime expiresAt,
        boolean isActive
) {}