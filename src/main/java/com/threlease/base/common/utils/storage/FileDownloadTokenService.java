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
    private static final String FILE_ID_CLAIM = "fileId";
    private static final String FILE_PATH_CLAIM = "filePath";

    private final JwtProperties jwtProperties;

    public FileDownloadTokenService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public String createToken(Long fileId, String filePath, long expireMinutes) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expireMinutes, ChronoUnit.MINUTES)))
                .claim(TOKEN_TYPE_CLAIM, TOKEN_TYPE)
                .claim(FILE_ID_CLAIM, fileId)
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

            Number fileIdNumber = claims.get(FILE_ID_CLAIM, Number.class);
            String filePath = claims.get(FILE_PATH_CLAIM, String.class);
            if (fileIdNumber == null || filePath == null || filePath.isBlank()) {
                return Optional.empty();
            }

            return Optional.of(new FileDownloadClaims(fileIdNumber.longValue(), filePath));
        } catch (JwtException e) {
            return Optional.empty();
        }
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getSecretKey()));
    }

    public record FileDownloadClaims(Long fileId, String filePath) {
    }
}
