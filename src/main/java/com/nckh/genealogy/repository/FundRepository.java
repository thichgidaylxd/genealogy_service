package com.nckh.genealogy.repository;

import com.nckh.genealogy.entity.Fund;
import com.nckh.genealogy.entity.TreeEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FundRepository extends JpaRepository<Fund, UUID> {
    List<Fund> findByTreeId(UUID treeId);

    boolean existsByName(String name);

    void deleteByTreeEvent_Id(UUID treeEventId);
}
