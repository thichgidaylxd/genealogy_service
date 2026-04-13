package com.nckh.genealogy.repository;

import com.nckh.genealogy.entity.FamilyChild;
import com.nckh.genealogy.entity.FamilyChildId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FamilyChildRepository extends JpaRepository<FamilyChild, FamilyChildId> {

    List<FamilyChild> findByFamilyId(UUID familyId);

    // Kiểm tra person đã là con trong bất kỳ family nào trong tree chưa
    @Query("""
            SELECT COUNT(fc) > 0 FROM FamilyChild fc
            JOIN TreePerson tp ON tp.person.id = fc.family.parent1.id
            WHERE tp.tree.id = :treeId
            AND tp.deletedAt IS NULL
            AND fc.person.id = :personId
            """)
    boolean existsAsChildInTree(@Param("personId") UUID personId, @Param("treeId") UUID treeId);

    // Lấy tất cả con của 1 family
    @Query("""
            SELECT fc FROM FamilyChild fc
            JOIN FETCH fc.person
            WHERE fc.family.id = :familyId
            """)
    List<FamilyChild> findChildrenByFamilyId(@Param("familyId") UUID familyId);


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM FamilyChild fc WHERE fc.person.id = :personId")
    void deleteByPersonId(@Param("personId") UUID personId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM FamilyChild fc WHERE fc.id.familyId = :familyId")
    void deleteByFamilyId(@Param("familyId") UUID familyId);

    @Modifying
    @Query("delete from FamilyChild fc where fc.person.id in :ids")
    void deleteByPersonIdIn(List<UUID> ids);
}