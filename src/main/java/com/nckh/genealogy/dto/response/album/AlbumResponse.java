package com.nckh.genealogy.dto.response.album;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlbumResponse {
    UUID id;
    UUID treeId;
    String name;
    String description;
    Integer mediaFileSize;
    // Constructor cho JPQL COUNT (trả về Long)
    public AlbumResponse(UUID id, UUID treeId, String name, String description, Long mediaFileSize) {
        this.id = id;
        this.treeId = treeId;
        this.name = name;
        this.description = description;
        this.mediaFileSize = mediaFileSize != null ? mediaFileSize.intValue() : 0;
    }
}
