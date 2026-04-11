package com.threlease.base.common.utils.storage;

import com.threlease.base.common.properties.app.jwt.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

@Service
public class FileDownloadTokenService {
    private static final String TOKEN_TYPE = "file-download";
    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String FILE_UUID_CLAIM = "fileUuid";
    private static final String FILE_PATH_CLAIM = "filePath";

    private final JwtProperties jwtProperties;

    public FileDownloadTokenService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public String createToken(String fileUuid, String filePath, long expireMinutes) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expireMinutes, ChronoUnit.MINUTES)))
                .claim(TOKEN_TYPE_CLAIM, TOKEN_TYPE)
                .claim(FILE_UUID_CLAIM, fileUuid)
                .claim(FILE_PATH_CLAIM, filePath)
                .signWith(getSigningKey())
                .compact();
    }

    public Optional<FileDownloadClaims> verify(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            if (!TOKEN_TYPE.equals(claims.get(TOKEN_TYPE_CLAIM, String.class))) {
                return Optional.empty();
            }

            String fileUuid = claims.get(FILE_UUID_CLAIM, String.class);
            String filePath = claims.get(FILE_PATH_CLAIM, String.class);
            if (fileUuid == null || fileUuid.isBlank() || filePath == null || filePath.isBlank()) {
                return Optional.empty();
            }

            return Optional.of(new FileDownloadClaims(fileUuid, filePath));
        } catch (JwtException e) {
            return Optional.empty();
        }
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getSecretKey()));
    }

    public record FileDownloadClaims(String fileUuid, String filePath) {
    }
}
