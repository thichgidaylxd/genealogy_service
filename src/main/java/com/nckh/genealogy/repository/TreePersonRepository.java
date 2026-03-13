package com.nckh.genealogy.repository;

import com.nckh.genealogy.entity.TreePerson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TreePersonRepository extends JpaRepository<TreePerson, UUID> {
    long countByTreeIdAndDeletedAtIsNull(UUID treeId);
    boolean existsByTreeIdAndPersonIdAndDeletedAtIsNull(UUID treeId, UUID personId);
    List<TreePerson> findByTreeIdAndDeletedAtIsNull(UUID treeId);
}