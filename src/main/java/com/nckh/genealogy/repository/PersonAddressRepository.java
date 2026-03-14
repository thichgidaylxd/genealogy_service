package com.nckh.genealogy.repository;

import com.nckh.genealogy.entity.PersonAddress;
import com.nckh.genealogy.entity.PersonAddressId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PersonAddressRepository extends JpaRepository<PersonAddress, PersonAddressId> {

    @EntityGraph(attributePaths = {"address", "addressType"})
    List<PersonAddress> findByPersonId(UUID personId);

    @EntityGraph(attributePaths = {"address", "addressType"})
    Optional<PersonAddress> findByPersonIdAndAddressId(UUID personId, UUID addressId);

    boolean existsByPersonIdAndIsPrimary(UUID personId, Short isPrimary);

    boolean existsByAddressId(UUID addressId);
}