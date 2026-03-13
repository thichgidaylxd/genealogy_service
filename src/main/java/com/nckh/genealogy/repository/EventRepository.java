package com.nckh.genealogy.repository;

import com.nckh.genealogy.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {


    // Lấy tất cả event của tree (qua tree_events)
    @Query("""
            SELECT e FROM Event e
            JOIN TreeEvent te ON te.event.id = e.id
            WHERE te.tree.id = :treeId
            ORDER BY e.startedAt DESC
            """)
    List<Event> findAllByTreeId(@Param("treeId") UUID treeId);

    // Lấy tất cả event của person (qua person_events)
    @Query("""
            SELECT e FROM Event e
            JOIN PersonEvent pe ON pe.event.id = e.id
            WHERE pe.person.id = :personId
            ORDER BY e.startedAt DESC
            """)
    List<Event> findAllByPersonId(@Param("personId") UUID personId);
}