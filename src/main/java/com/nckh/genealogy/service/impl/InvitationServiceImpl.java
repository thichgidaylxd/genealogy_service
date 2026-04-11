package com.nckh.genealogy.service.impl;

import com.nckh.genealogy.dto.request.invitation.CreateShareLinkRequest;
import com.nckh.genealogy.dto.request.invitation.SendInvitationRequest;
import com.nckh.genealogy.dto.response.invitation.ShareLinkResponse;
import com.nckh.genealogy.entity.*;
import com.nckh.genealogy.enums.InvitationStatus;
import com.nckh.genealogy.enums.TreeMemberRole;
import com.nckh.genealogy.enums.TreeMemberStatus;
import com.nckh.genealogy.exception.AppException;
import com.nckh.genealogy.exception.ErrorCode;
import com.nckh.genealogy.repository.*;
import com.nckh.genealogy.service.InvitationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvitationServiceImpl implements InvitationService {

    private final TreeInvitationRepository treeInvitationRepository;
    private final TreeShareLinkRepository treeShareLinkRepository;
    private final TreeRepository treeRepository;
    private final TreeMemberRepository treeMemberRepository;
    private final UserRepository userRepository;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    private static final int INVITATION_EXPIRY_DAYS = 7;

    // ==================== Invitation ====================

    @Override
    @Transactional
    public void sendInvitation(UUID treeId, UUID requesterId, SendInvitationRequest request) {
        requireTreeAdmin(requesterId, treeId);

        Tree tree = treeRepository.findById(treeId)
                .orElseThrow(() -> new AppException(ErrorCode.TREE_NOT_FOUND));
        User inviter = userRepository.findById(requesterId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (treeInvitationRepository.existsByTreeIdAndEmailAndStatus(
                treeId, request.email(), InvitationStatus.PENDING)) {
            throw new AppException(ErrorCode.CONFLICT, "Đã gửi lời mời cho email này rồi");
        }

        String token = UUID.randomUUID().toString();

        TreeInvitation invitation = TreeInvitation.builder()
                .tree(tree)
                .invitedBy(inviter)
                .email(request.email())
                .role(request.role())
                .inviteToken(token)
                .expiresAt(LocalDateTime.now().plusDays(INVITATION_EXPIRY_DAYS))
                .status(InvitationStatus.PENDING)
                .build();
        treeInvitationRepository.save(invitation);

        String inviteUrl = frontendUrl + "/invite/accept?token=" + token;
        log.info("Invitation URL for {}: {}", request.email(), inviteUrl);
    }

    @Override
    @Transactional
    public void acceptInvitation(String token, UUID userId) {
        TreeInvitation invitation = treeInvitationRepository.findByInviteToken(token)
                .orElseThrow(() -> new AppException(ErrorCode.TREE_INVITATION_NOT_FOUND));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new AppException(ErrorCode.TREE_INVITATION_ALREADY_USED);
        }
        if (LocalDateTime.now().isAfter(invitation.getExpiresAt())) {
            invitation.setStatus(InvitationStatus.EXPIRED);
            treeInvitationRepository.save(invitation);
            throw new AppException(ErrorCode.TREE_INVITATION_EXPIRED);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (treeMemberRepository.existsByUserIdAndTreeIdAndStatusIsActive(
                userId, invitation.getTree().getId())) {
            throw new AppException(ErrorCode.TREE_MEMBER_ALREADY_EXISTS);
        }

        TreeMember member = TreeMember.builder()
                .user(user)
                .tree(invitation.getTree())
                .invitedBy(invitation.getInvitedBy())
                .role(invitation.getRole())
                .status(TreeMemberStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .build();
        treeMemberRepository.save(member);

        invitation.setStatus(InvitationStatus.ACCEPTED);
        treeInvitationRepository.save(invitation);
    }

    // ==================== Share Link ====================

    @Override
    @Transactional
    public ShareLinkResponse createShareLink(UUID treeId, UUID requesterId, CreateShareLinkRequest request) {
        requireTreeAdmin(requesterId, treeId);

        Tree tree = treeRepository.findById(treeId)
                .orElseThrow(() -> new AppException(ErrorCode.TREE_NOT_FOUND));
        User creator = userRepository.findById(requesterId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // DB: expires_at NOT NULL → bắt buộc phải có
        if (request.expiredAt() == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Thời hạn link không được để trống");
        }

        String token = UUID.randomUUID().toString();

        TreeShareLink shareLink = TreeShareLink.builder()
                .tree(tree)
                .createdBy(creator)
                .shareToken(token)
                .permission(request.permission())
                .expiresAt(request.expiredAt())
                .build();
        treeShareLinkRepository.save(shareLink);

        return toShareLinkResponse(shareLink);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShareLinkResponse> getActiveShareLinks(UUID treeId, UUID requesterId) {
        requireTreeAdmin(requesterId, treeId);
        return treeShareLinkRepository
                .findActiveByTreeId(treeId, LocalDateTime.now())
                .stream()
                .map(this::toShareLinkResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UUID getTreeIdByShareToken(String token) {
        TreeShareLink shareLink = treeShareLinkRepository.findByShareToken(token)
                .orElseThrow(() -> new AppException(ErrorCode.TREE_SHARE_LINK_NOT_FOUND));

        if (LocalDateTime.now().isAfter(shareLink.getExpiresAt())) {
            throw new AppException(ErrorCode.TREE_SHARE_LINK_EXPIRED);
        }
        return shareLink.getTree().getId();
    }

    @Override
    @Transactional
    public void revokeShareLink(UUID treeId, UUID shareLinkId, UUID requesterId) {
        requireTreeAdmin(requesterId, treeId);
        TreeShareLink shareLink = treeShareLinkRepository.findById(shareLinkId)
                .orElseThrow(() -> new AppException(ErrorCode.TREE_SHARE_LINK_NOT_FOUND));

        if (!shareLink.getTree().getId().equals(treeId)) {
            throw new AppException(ErrorCode.TREE_SHARE_LINK_NOT_FOUND);
        }
        // DB không có is_active → xóa hẳn
        treeShareLinkRepository.deleteById(shareLinkId);
    }

    // ==================== Helpers ====================

    private void requireTreeAdmin(UUID userId, UUID treeId) {
        TreeMember member = treeMemberRepository
                .findByUserIdAndTreeIdAndStatus(userId, treeId, TreeMemberStatus.ACTIVE)
                .orElseThrow(() -> new AppException(ErrorCode.TREE_ACCESS_DENIED));
        if (member.getRole().ordinal() < TreeMemberRole.ADMIN.ordinal()) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }
    }

    private ShareLinkResponse toShareLinkResponse(TreeShareLink shareLink) {
        String shareUrl = frontendUrl + "/share?token=" + shareLink.getShareToken();
        return new ShareLinkResponse(
                shareLink.getId(),
                shareUrl,
                shareLink.getPermission(),
                shareLink.getExpiresAt(),
                LocalDateTime.now().isBefore(shareLink.getExpiresAt())
        );
    }
}