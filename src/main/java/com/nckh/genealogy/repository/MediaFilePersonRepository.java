package com.nckh.genealogy.repository;

import com.nckh.genealogy.entity.MediaFilePerson;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MediaFilePersonRepository extends JpaRepository<MediaFilePerson, UUID> {

    @EntityGraph(attributePaths = {"mediaFile", "mediaFileType"})
    List<MediaFilePerson> findByPersonId(UUID personId);

    void deleteByMediaFileId(UUID mediaFileId);

    void deleteByPersonIdIn(List<UUID> personIds);
}