package com.nckh.genealogy.repository;

import com.nckh.genealogy.entity.RoleInEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RoleInEventRepository extends JpaRepository<RoleInEvent, UUID> {
}