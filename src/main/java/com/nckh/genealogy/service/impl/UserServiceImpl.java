package com.nckh.genealogy.service.impl;

import com.nckh.genealogy.dto.request.user.ChangePasswordRequest;
import com.nckh.genealogy.dto.request.user.UpdateUserRequest;
import com.nckh.genealogy.dto.response.user.UserResponse;
import com.nckh.genealogy.entity.User;
import com.nckh.genealogy.exception.AppException;
import com.nckh.genealogy.exception.ErrorCode;
import com.nckh.genealogy.mapper.UserMapper;import com.nckh.genealogy.repository.UserRepository;
import com.nckh.genealogy.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getMe(UUID userId) {
        User user = findUserById(userId);
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateMe(UUID userId, UpdateUserRequest request) {
        User user = findUserById(userId);

        if (StringUtils.hasText(request.firstName())) {
            user.setFirstName(request.firstName());
        }
        if (StringUtils.hasText(request.lastName())) {
            user.setLastName(request.lastName());
        }
        if (StringUtils.hasText(request.phoneNumber())) {
            // Kiểm tra phone không trùng với người khác
            if (userRepository.existsByPhoneNumberAndIdNot(request.phoneNumber(), userId)) {
                throw new AppException(ErrorCode.PHONE_ALREADY_EXISTS);
            }
            user.setPhoneNumber(request.phoneNumber());
        }
        if (request.gender() != null) {
            user.setGender(request.gender());
        }
        if (request.dateOfBirth() != null) {
            user.setDateOfBirth(request.dateOfBirth());
        }
        if (StringUtils.hasText(request.avatarUrl())) {
            user.setAvatarUrl(request.avatarUrl());
        }

        // Cập nhật fullName nếu có thay đổi firstName hoặc lastName
        String lastName = StringUtils.hasText(request.lastName())
                ? request.lastName() : user.getLastName();
        String firstName = StringUtils.hasText(request.firstName())
                ? request.firstName() : user.getFirstName();
        user.setFullName(lastName + " " + firstName);

        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Mật khẩu xác nhận không khớp");
        }

        User user = findUserById(userId);

        if (!passwordEncoder.matches(request.oldPassword(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Mật khẩu cũ không đúng");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID userId) {
        return userMapper.toResponse(findUserById(userId));
    }

    // ==================== Helper ====================
    private User findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }
}