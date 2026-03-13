package com.nckh.genealogy.service.impl;

import com.nckh.genealogy.entity.Role;
import com.nckh.genealogy.entity.User;
import com.nckh.genealogy.enums.UserStatus;
import com.nckh.genealogy.exception.AppException;
import com.nckh.genealogy.exception.ErrorCode;
import com.nckh.genealogy.repository.RoleRepository;
import com.nckh.genealogy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2UserServiceImpl extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");

        // Nếu user chưa tồn tại → tạo mới
        userRepository.findByEmail(email).orElseGet(() -> {
            Role role = roleRepository.findByName("USER")
                    .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

            // Tạo username tự động từ email
            String baseUsername = email.split("@")[0];
            String userName = generateUniqueUsername(baseUsername);

            String[] nameParts = name != null ? name.split(" ", 2) : new String[]{"", ""};
            String firstName = nameParts.length > 1 ? nameParts[1] : nameParts[0];
            String lastName = nameParts.length > 1 ? nameParts[0] : "";

            User newUser = User.builder()
                    .role(role)
                    .userName(userName)
                    .email(email)
                    .firstName(firstName)
                    .lastName(lastName)
                    .fullName(name != null ? name : "")
                    .avatarUrl(picture)
                    .status(UserStatus.ACTIVE)
                    .build();

            log.info("Created new user via OAuth2: {}", email);
            return userRepository.save(newUser);
        });

        return oAuth2User;
    }

    private String generateUniqueUsername(String base) {
        String candidate = base;
        while (userRepository.existsByUserName(candidate)) {
            candidate = base + "_" + UUID.randomUUID().toString().substring(0, 6);
        }
        return candidate;
    }
}