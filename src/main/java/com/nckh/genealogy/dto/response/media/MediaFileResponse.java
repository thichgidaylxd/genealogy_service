package com.nckh.genealogy.dto.response.media;

import java.util.UUID;

public record MediaFileResponse(
        UUID id,
        String fileUrl,
        String fileName,
        Long fileSize,
        String mediaFileType,
        String description
) {}