package com.nckh.genealogy.service.impl;

import com.nckh.genealogy.dto.response.media.MediaFileResponse;
import com.nckh.genealogy.entity.*;
import com.nckh.genealogy.enums.TreeMemberStatus;
import com.nckh.genealogy.exception.AppException;
import com.nckh.genealogy.exception.ErrorCode;
import com.nckh.genealogy.repository.*;
import com.nckh.genealogy.service.CloudinaryService;
import com.nckh.genealogy.service.MediaFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MediaFileServiceImpl implements MediaFileService {

    private final CloudinaryService cloudinaryService;
    private final MediaFileRepository mediaFileRepository;
    private final MediaFileTypeRepository mediaFileTypeRepository;
    private final MediaFilePersonRepository mediaFilePersonRepository;
    private final TreeMediaFileRepository treeMediaFileRepository;
    private final TreeMemberRepository treeMemberRepository;
    private final TreePersonRepository treePersonRepository;
    private final PersonRepository personRepository;

    @Override
    @Transactional
    public MediaFileResponse uploadToTree(UUID treeId, UUID requesterId, MultipartFile file,
                                          UUID mediaFileTypeId, String description) {
        requireTreeMember(requesterId, treeId);
        MediaFileType mediaFileType = findMediaFileType(mediaFileTypeId);

        String[] uploaded = cloudinaryService.upload(file, "genealogy/trees/" + treeId);
        MediaFile mediaFile = saveMediaFile(uploaded, file, description);

        Tree tree = new Tree();
        tree.setId(treeId);

        TreeMediaFile treeMediaFile = TreeMediaFile.builder()
                .tree(tree)
                .mediaFile(mediaFile)
                .mediaFileType(mediaFileType)
                .description(description != null ? description : "")
                .build();
        treeMediaFileRepository.save(treeMediaFile);

        return toResponse(mediaFile, mediaFileType.getName());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MediaFileResponse> getTreeMediaFiles(UUID treeId, UUID requesterId) {
        requireTreeMember(requesterId, treeId);
        return treeMediaFileRepository.findByTreeId(treeId).stream()
                .map(tm -> toResponse(tm.getMediaFile(), tm.getMediaFileType().getName()))
                .toList();
    }

    @Override
    @Transactional
    public MediaFileResponse uploadToPerson(UUID treeId, UUID personId, UUID requesterId,
                                            MultipartFile file, UUID mediaFileTypeId, String description) {
        requireTreeMember(requesterId, treeId);
        if (!treePersonRepository.existsByTreeIdAndPersonIdAndDeletedAtIsNull(treeId, personId)) {
            throw new AppException(ErrorCode.PERSON_NOT_FOUND);
        }

        MediaFileType mediaFileType = findMediaFileType(mediaFileTypeId);
        Person person = personRepository.findByIdAndDeletedAtIsNull(personId)
                .orElseThrow(() -> new AppException(ErrorCode.PERSON_NOT_FOUND));

        String[] uploaded = cloudinaryService.upload(file, "genealogy/persons/" + personId);
        MediaFile mediaFile = saveMediaFile(uploaded, file, description);

        MediaFilePerson mediaFilePerson = MediaFilePerson.builder()
                .mediaFile(mediaFile)
                .person(person)
                .mediaFileType(mediaFileType)
                .description(description != null ? description : "")
                .build();
        mediaFilePersonRepository.save(mediaFilePerson);

        return toResponse(mediaFile, mediaFileType.getName());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MediaFileResponse> getPersonMediaFiles(UUID treeId, UUID personId, UUID requesterId) {
        requireTreeMember(requesterId, treeId);
        return mediaFilePersonRepository.findByPersonId(personId).stream()
                .map(mp -> toResponse(mp.getMediaFile(), mp.getMediaFileType().getName()))
                .toList();
    }

    @Override
    @Transactional
    public void deleteMediaFile(UUID treeId, UUID mediaFileId, UUID requesterId) {
        requireTreeMember(requesterId, treeId);
        MediaFile mediaFile = mediaFileRepository.findById(mediaFileId)
                .orElseThrow(() -> new AppException(ErrorCode.MEDIA_FILE_NOT_FOUND));

        // Parse publicId từ Cloudinary URL để xóa
        // URL dạng: https://res.cloudinary.com/{cloud}/image/upload/v123/{folder}/{publicId}.jpg
        String publicId = extractPublicId(mediaFile.getFileUrl());
        cloudinaryService.delete(publicId);

        mediaFileRepository.deleteById(mediaFileId);
    }

    // ==================== Helpers ====================

    private MediaFile saveMediaFile(String[] uploaded, MultipartFile file, String description) {
        // uploaded[0] = url, uploaded[1] = publicId (không lưu vào DB — parse lại từ URL khi cần)
        String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
        String fileType = resolveFileType(contentType);

        MediaFile mediaFile = MediaFile.builder()
                .fileUrl(uploaded[0])
                .fileName(file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown")
                .fileType(fileType)
                .fileSize(file.getSize())
                .mimeType(contentType)
                .title(file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown")
                .description(description != null ? description : "")
                .build();
        return mediaFileRepository.save(mediaFile);
    }

    /**
     * Map MIME type → file_type_enum value của PostgreSQL
     * (image / video / audio / document / other)
     */
    private String resolveFileType(String mimeType) {
        if (mimeType.startsWith("image/")) return "image";
        if (mimeType.startsWith("video/")) return "video";
        if (mimeType.startsWith("audio/")) return "audio";
        if (mimeType.equals("application/pdf")
                || mimeType.startsWith("application/msword")
                || mimeType.startsWith("application/vnd")) return "document";
        return "other";
    }

    /**
     * Parse Cloudinary publicId từ URL
     * URL: https://res.cloudinary.com/{cloud}/image/upload/v{ver}/{publicId}.{ext}
     */
    private String extractPublicId(String url) {
        try {
            // Lấy phần sau "/upload/"
            int uploadIdx = url.indexOf("/upload/");
            if (uploadIdx == -1) return url;
            String afterUpload = url.substring(uploadIdx + 8);
            // Bỏ version nếu có (v123456/)
            if (afterUpload.startsWith("v") && afterUpload.contains("/")) {
                afterUpload = afterUpload.substring(afterUpload.indexOf("/") + 1);
            }
            // Bỏ phần mở rộng file
            int dotIdx = afterUpload.lastIndexOf(".");
            return dotIdx != -1 ? afterUpload.substring(0, dotIdx) : afterUpload;
        } catch (Exception e) {
            return url;
        }
    }

    private MediaFileType findMediaFileType(UUID id) {
        return mediaFileTypeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MEDIA_FILE_TYPE_NOT_FOUND));
    }

    private void requireTreeMember(UUID userId, UUID treeId) {
        if (!treeMemberRepository.existsByUserIdAndTreeIdAndStatus(userId, treeId, TreeMemberStatus.ACTIVE)) {
            throw new AppException(ErrorCode.TREE_ACCESS_DENIED);
        }
    }

    private MediaFileResponse toResponse(MediaFile mediaFile, String mediaFileTypeName) {
        return new MediaFileResponse(
                mediaFile.getId(),
                mediaFile.getFileUrl(),
                mediaFile.getFileName(),
                mediaFile.getFileSize(),
                mediaFileTypeName,
                mediaFile.getDescription()
        );
    }
}