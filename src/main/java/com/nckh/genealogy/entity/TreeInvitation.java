package com.nckh.genealogy.entity;

import com.nckh.genealogy.enums.InvitationStatus;
import com.nckh.genealogy.enums.TreeMemberRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tree_invitations")
@EntityListeners(AuditingEntityListener.class)
public class TreeInvitation {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tree_id", nullable = false)
    private Tree tree;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by", nullable = false)
    private User invitedBy;

    // DB dùng tên cột "email" (không phải invited_email)
    @Column(name = "email", nullable = false, length = 255)
    private String email;

    // DB dùng "invite_token" (không phải token)
    @Column(name = "invite_token", nullable = false, length = 255)
    private String inviteToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InvitationStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private TreeMemberRole role;

    // DB dùng "expires_at" (không phải expired_at)
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;
}