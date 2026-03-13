package com.nckh.genealogy.repository;

import com.nckh.genealogy.entity.TreeMember;
import com.nckh.genealogy.enums.TreeMemberRole;
import com.nckh.genealogy.enums.TreeMemberStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TreeMemberRepository extends JpaRepository<TreeMember, UUID> {

    @EntityGraph(attributePaths = {"user", "tree"})
    Optional<TreeMember> findByUserIdAndTreeId(UUID userId, UUID treeId);

    @EntityGraph(attributePaths = {"user"})
    List<TreeMember> findByTreeIdAndStatus(UUID treeId, TreeMemberStatus status);

    @EntityGraph(attributePaths = {"tree"})
    List<TreeMember> findByUserIdAndStatus(UUID userId, TreeMemberStatus status);

    @EntityGraph(attributePaths = {"user", "tree"})
    Optional<TreeMember> findByUserIdAndTreeIdAndStatus(UUID userId, UUID treeId, TreeMemberStatus status);

    boolean existsByUserIdAndTreeIdAndStatus(UUID userId, UUID treeId, TreeMemberStatus status);

    long countByTreeIdAndStatus(UUID treeId, TreeMemberStatus status);
}