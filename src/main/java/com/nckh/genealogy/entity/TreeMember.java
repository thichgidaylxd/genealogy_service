package com.nckh.genealogy.entity;

import com.nckh.genealogy.enums.TreeMemberRole;
import com.nckh.genealogy.enums.TreeMemberStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tree_members")
public class TreeMember extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tree_id", nullable = false)
    private Tree tree;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by", nullable = false)
    private User invitedBy;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "role", nullable = false)
    private TreeMemberRole role;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status", nullable = false)
    private TreeMemberStatus status;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;
}