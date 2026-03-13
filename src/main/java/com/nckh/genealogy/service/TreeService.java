package com.nckh.genealogy.service;

import com.nckh.genealogy.dto.request.tree.CreateTreeRequest;
import com.nckh.genealogy.dto.request.tree.UpdateTreeRequest;
import com.nckh.genealogy.dto.response.tree.TreeMemberResponse;
import com.nckh.genealogy.dto.response.tree.TreeResponse;
import com.nckh.genealogy.enums.TreeMemberRole;

import java.util.List;
import java.util.UUID;

public interface TreeService {
    TreeResponse createTree(UUID userId, CreateTreeRequest request);
    TreeResponse getTreeById(UUID treeId, UUID userId);
    TreeResponse getTreePublic(UUID treeId);
    TreeResponse updateTree(UUID treeId, UUID userId, UpdateTreeRequest request);
    void deleteTree(UUID treeId, UUID userId);
    List<TreeResponse> getMyTrees(UUID userId);

    // Tree members
    List<TreeMemberResponse> getTreeMembers(UUID treeId, UUID userId);
    void updateMemberRole(UUID treeId, UUID targetUserId, UUID requesterId, TreeMemberRole newRole);
    void removeMember(UUID treeId, UUID targetUserId, UUID requesterId);
    void leaveTree(UUID treeId, UUID userId);
}