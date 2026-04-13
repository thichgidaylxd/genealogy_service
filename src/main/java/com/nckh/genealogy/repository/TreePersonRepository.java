package com.nckh.genealogy.repository;

import com.nckh.genealogy.entity.TreePerson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TreePersonRepository extends JpaRepository<TreePerson, UUID> {
    long countByTreeIdAndDeletedAtIsNull(UUID treeId);

    boolean existsByTreeIdAndPersonIdAndDeletedAtIsNull(UUID treeId, UUID personId);

    List<TreePerson> findByTreeIdAndDeletedAtIsNull(UUID treeId);

    void deleteByTreeId(UUID treeId);

    @Modifying
    @Query("DELETE FROM TreePerson tp WHERE tp.person.id = :personId")
    void deleteByPersonId(@Param("personId") UUID personId);
}