package com.nckh.genealogy.repository;

import com.nckh.genealogy.entity.TreeShareLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TreeShareLinkRepository extends JpaRepository<TreeShareLink, UUID> {

    Optional<TreeShareLink> findByShareToken(String shareToken);

    // Lấy các link chưa hết hạn
    @Query("""
            SELECT s FROM TreeShareLink s
            WHERE s.tree.id = :treeId
            AND s.expiresAt > :now
            """)
    List<TreeShareLink> findActiveByTreeId(@Param("treeId") UUID treeId,
                                           @Param("now") LocalDateTime now);
}