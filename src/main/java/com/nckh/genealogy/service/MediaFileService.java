package com.nckh.genealogy.service;

import com.nckh.genealogy.dto.response.media.MediaFileResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface MediaFileService {
    // Upload cho tree
    MediaFileResponse uploadToTree(UUID treeId, UUID requesterId, MultipartFile file,
                                   UUID mediaFileTypeId, String description);
    List<MediaFileResponse> getTreeMediaFiles(UUID treeId, UUID requesterId);

    // Upload cho person
    MediaFileResponse uploadToPerson(UUID treeId, UUID personId, UUID requesterId,
                                     MultipartFile file, UUID mediaFileTypeId, String description);
    List<MediaFileResponse> getPersonMediaFiles(UUID treeId, UUID personId, UUID requesterId);

    // Xóa file
    void deleteMediaFile(UUID treeId, UUID mediaFileId, UUID requesterId);
}