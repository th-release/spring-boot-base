package com.threlease.base.common.properties.app.jwt;

import io.jsonwebtoken.io.Decoders;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.security.SecureRandom;
import java.util.Base64;

@Configuration
@ConfigurationProperties("app.jwt")
@Getter
@Setter
public class JwtProperties {
    private static final int MIN_HS512_KEY_BYTES = 64;

    private String secretKey;

    @PostConstruct
    public void validateSecretKey() {
        if (secretKey == null || secretKey.isBlank()) {
            throw invalidKeyException("JWT secret key must not be blank");
        }

        try {
            byte[] decoded = Decoders.BASE64.decode(secretKey);
            if (decoded.length < MIN_HS512_KEY_BYTES) {
                throw invalidKeyException("JWT secret key must be at least 64 bytes after Base64 decoding");
            }
        } catch (IllegalArgumentException e) {
            throw invalidKeyException("JWT secret key must be a valid Base64-encoded value");
        }
    }

    private IllegalStateException invalidKeyException(String reason) {
        return new IllegalStateException(reason + ". Recommended JWT_SECRET_KEY: " + generateRecommendedSecretKey());
    }

    private String generateRecommendedSecretKey() {
        byte[] key = new byte[MIN_HS512_KEY_BYTES];
        new SecureRandom().nextBytes(key);
        return Base64.getEncoder().encodeToString(key);
    }
}
