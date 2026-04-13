package com.nckh.genealogy.repository;

import com.nckh.genealogy.entity.TreeInvitation;
import com.nckh.genealogy.enums.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TreeInvitationRepository extends JpaRepository<TreeInvitation, UUID> {

    Optional<TreeInvitation> findByInviteToken(String inviteToken);

    boolean existsByTreeIdAndEmailAndStatus(UUID treeId, String email, InvitationStatus status);

    void deleteByTreeId(UUID treeId);
}