package com.threlease.base.functions.auth;

import com.threlease.base.common.enums.AuthStatuses;
import com.threlease.base.common.enums.AuthTypes;
import com.threlease.base.entities.AuthEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthAccountFactory {
    private final PasswordEncoder passwordEncoder;

    public AuthEntity create(String username,
                             String nickname,
                             String email,
                             String rawPassword,
                             AuthTypes type,
                             AuthStatuses status) {
        return AuthEntity.builder()
                .username(trim(username, 24))
                .nickname(trim(nickname, 36))
                .email(trim(email, 255))
                .password(passwordEncoder.encode(rawPassword))
                .salt("")
                .type(type == null ? AuthTypes.GENERAL : type)
                .status(status == null ? AuthStatuses.ACTIVE : status)
                .build();
    }

    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    private String trim(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.substring(0, Math.min(value.length(), maxLength));
    }
}
