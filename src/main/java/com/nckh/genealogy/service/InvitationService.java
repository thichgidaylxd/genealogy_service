package com.nckh.genealogy.service;

import com.nckh.genealogy.dto.request.invitation.CreateShareLinkRequest;
import com.nckh.genealogy.dto.request.invitation.SendInvitationRequest;
import com.nckh.genealogy.dto.response.invitation.ShareLinkResponse;

import java.util.List;
import java.util.UUID;

public interface InvitationService {

    // Gửi lời mời qua email
    void sendInvitation(UUID treeId, UUID requesterId, SendInvitationRequest request);

    // Chấp nhận lời mời (người được mời click link trong email)
    void acceptInvitation(String token);

    // Tạo share link
    ShareLinkResponse createShareLink(UUID treeId, UUID requesterId, CreateShareLinkRequest request);

    // Lấy danh sách share link đang active
    List<ShareLinkResponse> getActiveShareLinks(UUID treeId, UUID requesterId);

    // Xem thông tin tree qua share link (không cần đăng nhập)
    UUID getTreeIdByShareToken(String token);

    // Thu hồi share link
    void revokeShareLink(UUID treeId, UUID shareLinkId, UUID requesterId);
}