package com.nckh.genealogy.service;

import com.nckh.genealogy.dto.request.album.CreateAlbumRequest;
import com.nckh.genealogy.dto.request.album.UpdateAlbumRequest;
import com.nckh.genealogy.dto.response.album.AlbumResponse;

import java.util.List;
import java.util.UUID;

public interface AlbumService {
    List<AlbumResponse> findAll(UUID treeId);

    AlbumResponse createAlbum(UUID treeId, UUID requesterId, CreateAlbumRequest request);

    AlbumResponse updateAlbum(UpdateAlbumRequest request);

    AlbumResponse deleteAlbum(UUID id);
}
