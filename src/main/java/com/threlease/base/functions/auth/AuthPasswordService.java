package com.threlease.base.functions.auth;

import com.threlease.base.entities.AuthEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class AuthPasswordService {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final PasswordEncoder passwordEncoder;

    public EncodedPassword encode(String rawPassword) {
        String salt = generateSalt();
        return new EncodedPassword(passwordEncoder.encode(preHash(rawPassword, salt)), salt);
    }

    public boolean matches(String rawPassword, AuthEntity auth) {
        if (auth == null || auth.getPassword() == null) {
            return false;
        }
        if (auth.getSalt() == null || auth.getSalt().isBlank()) {
            return passwordEncoder.matches(rawPassword, auth.getPassword());
        }
        return passwordEncoder.matches(preHash(rawPassword, auth.getSalt()), auth.getPassword());
    }

    private String generateSalt() {
        byte[] salt = new byte[32];
        SECURE_RANDOM.nextBytes(salt);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(salt);
    }

    private String preHash(String rawPassword, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest((rawPassword + ":" + salt).getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashed);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to pre-hash password", e);
        }
    }

    public record EncodedPassword(String passwordHash, String salt) {
    }
}
