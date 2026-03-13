package com.nckh.genealogy.mapper;

import com.nckh.genealogy.dto.response.user.UserResponse;
import com.nckh.genealogy.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "role", expression = "java(user.getRole().getName())")
    UserResponse toResponse(User user);
}