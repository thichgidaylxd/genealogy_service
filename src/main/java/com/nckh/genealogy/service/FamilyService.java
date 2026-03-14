package com.nckh.genealogy.service;

import com.nckh.genealogy.dto.request.family.AddChildRequest;
import com.nckh.genealogy.dto.request.family.AddParentRequest;
import com.nckh.genealogy.dto.request.family.AddSpouseRequest;
import com.nckh.genealogy.dto.request.person.CreatePersonRequest;
import com.nckh.genealogy.dto.response.family.FamilyResponse;
import com.nckh.genealogy.dto.response.family.PersonFamilyResponse;
import com.nckh.genealogy.dto.response.family.TreeGraphResponse;
import com.nckh.genealogy.dto.response.person.PersonResponse;

import java.util.UUID;

public interface FamilyService {

    // Thêm vợ/chồng mới cho person → tạo person mới + family
    FamilyResponse addSpouse(UUID treeId, UUID personId, UUID requesterId, AddSpouseRequest request);

    // Thêm cha/mẹ mới cho person → tạo person mới + gắn vào family
    FamilyResponse addParent(UUID treeId, UUID personId, UUID requesterId, AddParentRequest request);

    // Thêm con mới vào family → tạo person mới + gắn vào family_children
    FamilyResponse addChild(UUID treeId, UUID familyId, UUID requesterId, AddChildRequest request);

    // Xóa con khỏi family
    void removeChild(UUID treeId, UUID familyId, UUID personId, UUID requesterId);

    // Xóa family (soft delete)
    void deleteFamily(UUID treeId, UUID familyId, UUID requesterId);

    // Xem thông tin gia đình của 1 person
    PersonFamilyResponse getPersonFamily(UUID treeId, UUID personId, UUID requesterId);

    // Lấy toàn bộ cây gia phả để render
    TreeGraphResponse getTreeGraph(UUID treeId, UUID requesterId);

    TreeGraphResponse getTreeGraphPublic(UUID treeId); // không cần requesterId

    PersonResponse addFirstPersonIntoTree(UUID treeId, UUID requesterId, CreatePersonRequest request);
}