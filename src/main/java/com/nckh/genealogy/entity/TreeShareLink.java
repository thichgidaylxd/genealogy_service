package com.nckh.genealogy.entity;

import com.nckh.genealogy.enums.SharePermission;
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
@Table(name = "tree_share_links")
@EntityListeners(AuditingEntityListener.class)
public class TreeShareLink {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tree_id", nullable = false)
    private Tree tree;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    // DB dùng "share_token" (không phải token)
    @Column(name = "share_token", nullable = false, length = 255)
    private String shareToken;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "permission", nullable = false)
    private SharePermission permission;

    // DB: expires_at NOT NULL (không phải nullable)
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;
    // Không có updated_at, không có is_active trong DB
}