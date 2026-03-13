package com.nckh.genealogy.dto.response.tree;

import com.nckh.genealogy.enums.TreeMemberRole;

import java.time.LocalDateTime;
import java.util.UUID;

// Dùng cho danh sách — thông tin cơ bản
public record TreeResponse(
        UUID id,
        String name,
        String description,
        TreeMemberRole myRole,      // role của người đang request
        int totalMembers,
        int totalPersons,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}