package com.nckh.genealogy.dto.response.auth;

import java.util.UUID;

public record AuthResponse(
        UUID userId,
        String userName,
        String email,
        String fullName,
        String avatarUrl,
        String role,
        String accessToken,
        String tokenType
) {
    public static AuthResponse of(UUID userId, String userName, String email,
                                  String fullName, String avatarUrl,
                                  String role, String accessToken) {
        return new AuthResponse(userId, userName, email, fullName,
                avatarUrl, role, accessToken, "Bearer");
    }
}