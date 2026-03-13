package com.nckh.genealogy.dto.response.lookup;

import java.util.UUID;

public record LookupResponse(UUID id, String name, String description) {}