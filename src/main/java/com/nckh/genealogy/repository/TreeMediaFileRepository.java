package com.nckh.genealogy.repository;

import com.nckh.genealogy.entity.Album;
import com.nckh.genealogy.entity.TreeMediaFile;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TreeMediaFileRepository extends JpaRepository<TreeMediaFile, UUID> {

    @EntityGraph(attributePaths = {"mediaFile", "mediaFileType"})
    List<TreeMediaFile> findByTreeId(UUID treeId);
    List<TreeMediaFile> findByTreeIdAndAlbumId(UUID treeId, UUID albumId);

    void deleteByTreeId(UUID treeId);

    Integer countByAlbum(Album a);

    void deleteByAlbumId(UUID albumId);
}