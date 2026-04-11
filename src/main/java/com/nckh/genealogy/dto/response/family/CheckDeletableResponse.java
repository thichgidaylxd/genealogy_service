package com.nckh.genealogy.dto.response.family;

import java.util.List;
import java.util.UUID;

public record CheckDeletableResponse(
        boolean deletable,
        List<UUID> orphanedPersonIds,
        List<String> orphanedPersonNames,
        String message
) {}