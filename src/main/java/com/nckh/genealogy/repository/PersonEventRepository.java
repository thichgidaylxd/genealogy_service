package com.nckh.genealogy.repository;

import com.nckh.genealogy.entity.PersonEvent;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PersonEventRepository extends JpaRepository<PersonEvent, UUID> {

    @EntityGraph(attributePaths = {"person", "eventType", "address"})
    List<PersonEvent> findByEventId(UUID eventId);

    @EntityGraph(attributePaths = {"event", "eventType", "address"})
    List<PersonEvent> findByPersonId(UUID personId);

    boolean existsByPersonIdAndEventId(UUID personId, UUID eventId);

    boolean existsByAddressId(UUID addressId);

    boolean existsByPersonIdAndEventTypeId(UUID personId, UUID eventTypeId);

    void deleteByPersonIdIn(List<UUID> personIds);
}