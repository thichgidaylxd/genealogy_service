package com.nckh.genealogy.service;

import com.nckh.genealogy.dto.request.user.ChangePasswordRequest;
import com.nckh.genealogy.dto.request.user.UpdateUserRequest;
import com.nckh.genealogy.dto.response.user.UserResponse;

import java.util.UUID;

public interface UserService {
    UserResponse getMe(UUID userId);
    UserResponse updateMe(UUID userId, UpdateUserRequest request);
    void changePassword(UUID userId, ChangePasswordRequest request);
    UserResponse getUserById(UUID userId);
}