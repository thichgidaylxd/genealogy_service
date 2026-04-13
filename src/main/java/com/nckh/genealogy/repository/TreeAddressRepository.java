package com.nckh.genealogy.repository;

import com.nckh.genealogy.entity.TreeAddress;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TreeAddressRepository extends JpaRepository<TreeAddress, UUID> {

    @EntityGraph(attributePaths = {"address", "addressType"})
    List<TreeAddress> findByTreeId(UUID treeId);

    boolean existsByAddressId(UUID addressId);

    Optional<TreeAddress> findByTreeIdAndAddressId(UUID treeId, UUID treeAddressId);

    void deleteByAddressId(UUID addressId);

    void deleteByTreeId(UUID treeId);
}