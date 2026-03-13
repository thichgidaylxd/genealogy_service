package com.nckh.genealogy.dto.request.invitation;

import com.nckh.genealogy.enums.TreeMemberRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SendInvitationRequest(
        @NotBlank @Email(message = "Email không hợp lệ")
        String email,

        @NotNull(message = "Vai trò không được để trống")
        TreeMemberRole role
) {}