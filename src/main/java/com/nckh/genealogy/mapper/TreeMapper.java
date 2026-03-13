package com.nckh.genealogy.mapper;

import com.nckh.genealogy.dto.response.tree.TreeMemberResponse;
import com.nckh.genealogy.dto.response.tree.TreeResponse;
import com.nckh.genealogy.entity.Tree;
import com.nckh.genealogy.entity.TreeMember;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TreeMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", source = "user.userName")
    @Mapping(target = "fullName", source = "user.fullName")
    @Mapping(target = "avatarUrl", source = "user.avatarUrl")
    TreeMemberResponse toMemberResponse(TreeMember treeMember);

    TreeResponse toResponse(Tree tree);
}