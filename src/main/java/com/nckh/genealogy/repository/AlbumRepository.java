package com.nckh.genealogy.repository;

import com.nckh.genealogy.dto.response.album.AlbumResponse;
import com.nckh.genealogy.entity.Album;
import com.nckh.genealogy.entity.Tree;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AlbumRepository extends JpaRepository<Album, UUID> {
    Optional<Album> findByTree(Tree tree);


    @Query("""
    SELECT new com.nckh.genealogy.dto.response.album.AlbumResponse(
        a.id,
        a.tree.id,
        a.name,
        a.description,
        COUNT(m)
    )
    FROM Album a
    LEFT JOIN TreeMediaFile m ON m.album = a
    WHERE a.tree.id = :treeId
    GROUP BY a.id, a.tree.id, a.name, a.description
""")
    List<AlbumResponse> findAllAlbumResponsesByTreeId(@Param("treeId") UUID treeId);

    boolean existsByTree_IdAndName(UUID treeId, String name);

    void deleteByTreeId(UUID treeId);
}
