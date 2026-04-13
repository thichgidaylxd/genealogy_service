package com.nckh.genealogy.service.impl;

import com.nckh.genealogy.dto.request.album.CreateAlbumRequest;
import com.nckh.genealogy.dto.request.album.UpdateAlbumRequest;
import com.nckh.genealogy.dto.response.album.AlbumResponse;
import com.nckh.genealogy.entity.Album;
import com.nckh.genealogy.entity.Tree;
import com.nckh.genealogy.exception.AppException;
import com.nckh.genealogy.exception.ErrorCode;
import com.nckh.genealogy.repository.AlbumRepository;
import com.nckh.genealogy.repository.TreeMediaFileRepository;
import com.nckh.genealogy.repository.TreeMemberRepository;
import com.nckh.genealogy.repository.TreeRepository;
import com.nckh.genealogy.service.AlbumService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AlbumServiceImpl implements AlbumService {

    AlbumRepository albumRepository;
    TreeMediaFileRepository treeMediaFileRepository;
    TreeMemberRepository treeMemberRepository;
    TreeRepository treeRepository;

//    @Override
//    public List<AlbumResponse> findAll() {
//        return albumRepository
//                .findAll()
//                .stream()
//                .map( a -> AlbumResponse.builder()
//                        .id(a.getId())
//                        .treeId(a.getTree().getId())
//                        .name(a.getName())
//                        .description(a.getDescription())
//                        .mediaFileSize(treeMediaFileRepository.countByAlbum(a))
//                        .build()
//                )
//                .toList();
//    }

    @Override
    public List<AlbumResponse> findAll(UUID treeId) {
        return albumRepository.findAllAlbumResponsesByTreeId(treeId);
    }

    @Override
    public AlbumResponse createAlbum(UUID treeId, UUID requesterId, CreateAlbumRequest request) {

        Tree tree = treeRepository.findById(treeId)
                .orElseThrow(() -> new AppException(ErrorCode.TREE_NOT_FOUND));

        if(!treeMemberRepository.existsByUserIdAndTreeIdAndStatusIsActive(requesterId, treeId))
            throw new AppException(ErrorCode.TREE_ACCESS_DENIED);

        if(albumRepository.existsByTree_IdAndName(treeId ,request.getName()))
            throw new AppException(ErrorCode.ALBUM_ALREADY_EXISTS);

        Album album = new Album();
        album.setName(request.getName());
        album.setDescription(request.getDescription());
        album.setTree(tree);
        album = albumRepository.save(album);

        return AlbumResponse.builder()
                .id(album.getId())
                .name(album.getName())
                .description(album.getDescription())
                .treeId(album.getTree().getId())
                .mediaFileSize(0)
                .build();
    }

    @Override
    public AlbumResponse updateAlbum(UpdateAlbumRequest request) {
        Album album = albumRepository.findById(request.getId())
                .orElseThrow(() -> new AppException(ErrorCode.ALBUM_NOT_FOUND));

        // Kiểm tra trùng tên trong cùng tree (trừ chính album này)
        if (albumRepository.existsByTree_IdAndName(album.getTree().getId(), request.getName())
                && !album.getName().equalsIgnoreCase(request.getName())) {
            throw new AppException(ErrorCode.ALBUM_ALREADY_EXISTS);
        }

        album.setName(request.getName());
        album.setDescription(request.getDescription());
        album = albumRepository.save(album);

        return AlbumResponse.builder()
                .id(album.getId())
                .treeId(album.getTree().getId())
                .name(album.getName())
                .description(album.getDescription())
                .mediaFileSize(treeMediaFileRepository.countByAlbum(album))
                .build();
    }

    @Override
    public AlbumResponse deleteAlbum(UUID id) {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ALBUM_NOT_FOUND));

        // Xóa tất cả media trong album trước
        // (nếu muốn xóa cả file trên Cloudinary thì inject MediaFileService ở đây)
        treeMediaFileRepository.deleteByAlbumId(id);

        albumRepository.delete(album);

        return AlbumResponse.builder()
                .id(album.getId())
                .treeId(album.getTree().getId())
                .name(album.getName())
                .description(album.getDescription())
                .mediaFileSize(0)
                .build();
    }

}
