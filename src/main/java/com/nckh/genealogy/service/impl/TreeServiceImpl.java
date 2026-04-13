package com.nckh.genealogy.service.impl;

import com.nckh.genealogy.dto.request.tree.CreateTreeRequest;
import com.nckh.genealogy.dto.request.tree.UpdateTreeRequest;
import com.nckh.genealogy.dto.response.tree.TreeMemberResponse;
import com.nckh.genealogy.dto.response.tree.TreeResponse;
import com.nckh.genealogy.entity.Tree;
import com.nckh.genealogy.entity.TreeMember;
import com.nckh.genealogy.entity.User;
import com.nckh.genealogy.enums.TreeMemberRole;
import com.nckh.genealogy.enums.TreeMemberStatus;
import com.nckh.genealogy.exception.AppException;
import com.nckh.genealogy.exception.ErrorCode;
import com.nckh.genealogy.mapper.TreeMapper;
import com.nckh.genealogy.repository.*;
import com.nckh.genealogy.service.TreeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TreeServiceImpl implements TreeService {

    private final TreeRepository treeRepository;
    private final TreeMemberRepository treeMemberRepository;
    private final TreePersonRepository treePersonRepository;
    private final UserRepository userRepository;
    private final TreeMapper treeMapper;

    private final TreeMediaFileRepository treeMediaFileRepository;
    private final TreeEventRepository treeEventRepository;

    private final TreeInvitationRepository treeInvitationRepository;
    private final TreeAddressRepository treeAddressRepository;

    private final TreeShareLinkRepository treeShareLinkRepository;

    private final AlbumRepository albumRepository;
    @Override
    @Transactional
    public TreeResponse createTree(UUID userId, CreateTreeRequest request) {
        User user = findUserById(userId);

        Tree tree = Tree.builder()
                .name(request.name())
                .description(request.description())
                .build();
        treeRepository.save(tree);

        // Người tạo tự động là OWNER
        TreeMember owner = TreeMember.builder()
                .user(user)
                .tree(tree)
                .invitedBy(user)
                .role(TreeMemberRole.OWNER)
                .status(TreeMemberStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .build();
        treeMemberRepository.save(owner);

        return buildTreeResponse(tree, TreeMemberRole.OWNER, 1, 0);
    }

    @Override
    @Transactional(readOnly = true)
    public TreeResponse getTreeById(UUID treeId, UUID userId) {
        Tree tree = findTreeById(treeId);
        TreeMember member = findActiveMember(userId, treeId);

        int totalMembers = (int) treeMemberRepository.countByTreeIdAndStatus(treeId, TreeMemberStatus.ACTIVE);
        int totalPersons = (int) treePersonRepository.countByTreeIdAndDeletedAtIsNull(treeId);

        return buildTreeResponse(tree, member.getRole(), totalMembers, totalPersons);
    }

    @Override
    @Transactional(readOnly = true)
    public TreeResponse getTreePublic(UUID treeId) {
        Tree tree = treeRepository.findById(treeId)
                .orElseThrow(() -> new AppException(ErrorCode.TREE_NOT_FOUND));
        return treeMapper.toResponse(tree);
    }

    @Override
    @Transactional
    public TreeResponse updateTree(UUID treeId, UUID userId, UpdateTreeRequest request) {
        Tree tree = findTreeById(treeId);
        TreeMember member = findActiveMember(userId, treeId);

        // Chỉ OWNER và ADMIN mới được sửa thông tin tree
        requireRole(member, TreeMemberRole.ADMIN);

        if (StringUtils.hasText(request.name())) {
            tree.setName(request.name());
        }
        if (request.description() != null) {
            tree.setDescription(request.description());
        }

        treeRepository.save(tree);

        int totalMembers = (int) treeMemberRepository.countByTreeIdAndStatus(treeId, TreeMemberStatus.ACTIVE);
        int totalPersons = (int) treePersonRepository.countByTreeIdAndDeletedAtIsNull(treeId);
        return buildTreeResponse(tree, member.getRole(), totalMembers, totalPersons);
    }

    @Transactional
    public void deleteTree(UUID treeId, UUID userId) {
        findTreeById(treeId);
        TreeMember member = findActiveMember(userId, treeId);

        if (member.getRole() != TreeMemberRole.OWNER) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        // ─── DELETE THEO THỨ TỰ FK (QUAN TRỌNG) ───

        // Invitations
        treeInvitationRepository.deleteByTreeId(treeId);

        // Share links
        treeShareLinkRepository.deleteByTreeId(treeId);

        // Members
        treeMemberRepository.deleteByTreeId(treeId);


        // Media
        treeMediaFileRepository.deleteByTreeId(treeId);

        albumRepository.deleteByTreeId(treeId);

        // Events
        treeEventRepository.deleteByTreeId(treeId);

        // Persons in tree
        treePersonRepository.deleteByTreeId(treeId);

        // Address (nếu có)
        treeAddressRepository.deleteByTreeId(treeId);

        // Cuối cùng mới delete tree
        treeRepository.deleteById(treeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TreeResponse> getMyTrees(UUID userId) {
        return treeMemberRepository.findByUserIdAndStatus(userId, TreeMemberStatus.ACTIVE)
                .stream()
                .map(member -> {
                    Tree tree = member.getTree();
                    int totalMembers = (int) treeMemberRepository
                            .countByTreeIdAndStatus(tree.getId(), TreeMemberStatus.ACTIVE);
                    int totalPersons = (int) treePersonRepository
                            .countByTreeIdAndDeletedAtIsNull(tree.getId());
                    return buildTreeResponse(tree, member.getRole(), totalMembers, totalPersons);
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TreeMemberResponse> getTreeMembers(UUID treeId, UUID userId) {
        // Kiểm tra người request có phải thành viên không
        findActiveMember(userId, treeId);

        return treeMemberRepository.findByTreeIdAndStatus(treeId, TreeMemberStatus.ACTIVE)
                .stream()
                .map(treeMapper::toMemberResponse)
                .toList();
    }

    @Override
    @Transactional
    public void updateMemberRole(UUID treeId, UUID targetUserId, UUID requesterId, TreeMemberRole newRole) {
        TreeMember requester = findActiveMember(requesterId, treeId);

        // Chỉ OWNER mới được thay đổi role
        if (requester.getRole() != TreeMemberRole.OWNER) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        // Không thể thay đổi role của chính mình
        if (requesterId.equals(targetUserId)) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Không thể thay đổi role của chính mình");
        }

        // Không thể set role OWNER cho người khác
        if (newRole == TreeMemberRole.OWNER) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Không thể chuyển quyền OWNER");
        }

        TreeMember target = findActiveMember(targetUserId, treeId);
        target.setRole(newRole);
        treeMemberRepository.save(target);
    }

    @Override
    @Transactional
    public void removeMember(UUID treeId, UUID targetUserId, UUID requesterId) {
        TreeMember requester = findActiveMember(requesterId, treeId);

        // Chỉ OWNER và ADMIN mới được xóa thành viên
        requireRole(requester, TreeMemberRole.ADMIN);

        // Không thể tự xóa chính mình qua endpoint này
        if (requesterId.equals(targetUserId)) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Dùng API leave để rời gia phả");
        }

        TreeMember target = findActiveMember(targetUserId, treeId);

        // ADMIN không thể xóa OWNER
        if (target.getRole() == TreeMemberRole.OWNER) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        target.setStatus(TreeMemberStatus.REMOVED);
        treeMemberRepository.save(target);
    }

    @Override
    @Transactional
    public void leaveTree(UUID treeId, UUID userId) {
        TreeMember member = findActiveMember(userId, treeId);

        // OWNER không thể rời nếu còn thành viên khác
        if (member.getRole() == TreeMemberRole.OWNER) {
            long count = treeMemberRepository.countByTreeIdAndStatus(treeId, TreeMemberStatus.ACTIVE);
            if (count > 1) {
                throw new AppException(ErrorCode.INVALID_REQUEST,
                        "Hãy chuyển quyền OWNER trước khi rời gia phả");
            }
        }

        member.setStatus(TreeMemberStatus.REMOVED);
        treeMemberRepository.save(member);
    }

    // ==================== Helpers ====================

    private Tree findTreeById(UUID treeId) {
        return treeRepository.findById(treeId)
                .orElseThrow(() -> new AppException(ErrorCode.TREE_NOT_FOUND));
    }

    private User findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private TreeMember findActiveMember(UUID userId, UUID treeId) {
        return treeMemberRepository.findByUserIdAndTreeId(userId, treeId)
                .filter(m -> m.getStatus() == TreeMemberStatus.ACTIVE)
                .orElseThrow(() -> new AppException(ErrorCode.TREE_ACCESS_DENIED));
    }

    // Kiểm tra role tối thiểu: ADMIN >= ADMIN, OWNER >= ADMIN
    private void requireRole(TreeMember member, TreeMemberRole minimumRole) {
        if (member.getRole().ordinal() < minimumRole.ordinal()) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }
    }

    private TreeResponse buildTreeResponse(Tree tree, TreeMemberRole myRole,
                                           int totalMembers, int totalPersons) {
        return new TreeResponse(
                tree.getId(),
                tree.getName(),
                tree.getDescription(),
                myRole,
                totalMembers,
                totalPersons,
                tree.getCreatedAt(),
                tree.getUpdatedAt()
        );
    }
}