package com.nckh.genealogy.dto.response.tree;

import com.nckh.genealogy.enums.TreeMemberRole;
import com.nckh.genealogy.enums.TreeMemberStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record TreeMemberResponse(
        UUID id,
        UUID userId,
        String userName,
        String fullName,
        String avatarUrl,
        TreeMemberRole role,
        TreeMemberStatus status,
        LocalDateTime joinedAt
) {}