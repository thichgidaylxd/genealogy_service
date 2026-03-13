package com.nckh.genealogy.service.impl;

import com.nckh.genealogy.dto.request.auth.LoginRequest;
import com.nckh.genealogy.dto.request.auth.RegisterRequest;
import com.nckh.genealogy.dto.response.auth.AuthResponse;
import com.nckh.genealogy.entity.Role;
import com.nckh.genealogy.entity.User;
import com.nckh.genealogy.enums.UserStatus;
import com.nckh.genealogy.exception.AppException;
import com.nckh.genealogy.exception.ErrorCode;
import com.nckh.genealogy.repository.RoleRepository;
import com.nckh.genealogy.repository.UserRepository;
import com.nckh.genealogy.service.AuthService;
import com.nckh.genealogy.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUserName(request.userName())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AppException(ErrorCode.ACCOUNT_DISABLED);
        }

        String token = jwtUtil.generateToken(
                user.getId(),
                user.getUserName(),
                user.getRole().getName()
        );

        return buildAuthResponse(user, token);
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUserName(request.userName())) {
            throw new AppException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        if (userRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new AppException(ErrorCode.PHONE_ALREADY_EXISTS);
        }

        Role role = roleRepository.findByName("USER")
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        String fullName = request.lastName() + " " + request.firstName();

        User user = User.builder()
                .role(role)
                .userName(request.userName())
                .passwordHash(passwordEncoder.encode(request.password()))
                .email(request.email())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .fullName(fullName)
                .phoneNumber(request.phoneNumber())
                .gender(request.gender())
                .dateOfBirth(request.dateOfBirth())
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(user);

        String token = jwtUtil.generateToken(
                user.getId(),
                user.getUserName(),
                role.getName()
        );

        return buildAuthResponse(user, token);
    }

    private AuthResponse buildAuthResponse(User user, String token) {
        return AuthResponse.of(
                user.getId(),
                user.getUserName(),
                user.getEmail(),
                user.getFullName(),
                user.getAvatarUrl(),
                user.getRole().getName(),
                token
        );
    }
}