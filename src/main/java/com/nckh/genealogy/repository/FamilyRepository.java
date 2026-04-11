package com.nckh.genealogy.repository;

import com.nckh.genealogy.entity.Family;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FamilyRepository extends JpaRepository<Family, UUID> {

    Optional<Family> findByIdAndDeletedAtIsNull(UUID id);

    // Tìm tất cả family mà person là parent1 hoặc parent2
    @Query("""
            SELECT f FROM Family f
            WHERE f.deletedAt IS NULL
            AND (f.parent1.id = :personId OR f.parent2.id = :personId)
            """)
    List<Family> findFamiliesAsParent(@Param("personId") UUID personId);

    // Kiểm tra 2 person đã có family chưa (theo cả 2 chiều)
    @Query("""
            SELECT COUNT(f) > 0 FROM Family f
            WHERE f.deletedAt IS NULL
            AND (
                (f.parent1.id = :p1 AND f.parent2.id = :p2)
                OR (f.parent1.id = :p2 AND f.parent2.id = :p1)
            )
            """)
    boolean existsByParents(@Param("p1") UUID p1, @Param("p2") UUID p2);

    // Tìm family mà person là con (qua family_children)
    @Query("""
            SELECT f FROM Family f
            JOIN FamilyChild fc ON fc.family.id = f.id
            WHERE f.deletedAt IS NULL
            AND fc.person.id = :personId
            """)
    Optional<Family> findFamilyAsChild(@Param("personId") UUID personId);

    // Lấy tất cả family trong tree (qua tree_persons)
    @Query("""
            SELECT DISTINCT f FROM Family f
            LEFT JOIN FETCH f.parent1
            LEFT JOIN FETCH f.parent2
            WHERE f.deletedAt IS NULL
            AND EXISTS (
                SELECT tp FROM TreePerson tp
                WHERE tp.tree.id = :treeId
                AND tp.deletedAt IS NULL
                AND (tp.person.id = f.parent1.id OR tp.person.id = f.parent2.id)
            )
            """)
    List<Family> findAllByTreeId(@Param("treeId") UUID treeId);

    @Query("""
    SELECT f
    FROM Family f
    WHERE 
        (
            f.parent1.id = :personId AND f.parent2 IS NULL
        )
        OR
        (
            f.parent2.id = :personId AND f.parent1 IS NULL
        )
""")
    List<Family> findSingleParentFamilies(UUID personId);
}