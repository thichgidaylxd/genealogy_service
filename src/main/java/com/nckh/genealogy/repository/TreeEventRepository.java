package com.nckh.genealogy.repository;

import com.nckh.genealogy.entity.TreeEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TreeEventRepository extends JpaRepository<TreeEvent, UUID> {

    boolean existsByTreeIdAndEventId(UUID treeId, UUID eventId);

    Optional<TreeEvent> findByTreeIdAndEventId(UUID treeId, UUID eventId);

    boolean existsByAddressId(UUID addressId);

    void deleteByTreeId(UUID treeId);
}
