package com.threlease.base.common.provider;

import com.threlease.base.common.properties.app.jwt.JwtProperties;
import com.threlease.base.entities.AuthEntity;
import com.threlease.base.repositories.auth.AuthRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtProvider {
    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String FAMILY_ID_CLAIM = "familyId";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    private final long accessTokenExp = 1000L * 60 * 60; // 1 hour
    private final long refreshTokenExp = 1000L * 60 * 60 * 24 * 14; // 14 days
    private final String issuer = "spring-boot-base";
    private final AuthRepository authRepository;
    private final JwtProperties jwtProperties;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getSecretKey()));
    }

    public String createAccessToken(String uuid) {
        return Jwts.builder()
                .subject(uuid)
                .issuer(issuer)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + accessTokenExp))
                .claim(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE)
                .signWith(getSigningKey())
                .compact();
    }

    public String createRefreshToken(String uuid, String tokenId, String familyId) {
        return Jwts.builder()
                .id(tokenId)
                .subject(uuid)
                .issuer(issuer)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExp))
                .claim(TOKEN_TYPE_CLAIM, REFRESH_TOKEN_TYPE)
                .claim(FAMILY_ID_CLAIM, familyId)
                .signWith(getSigningKey())
                .compact();
    }

    public Optional<Jws<Claims>> verify(String token) {
        if (token == null || token.isEmpty()) {
            return Optional.empty();
        }
        
        if (token.toLowerCase().startsWith("bearer ")) {
            token = token.substring(7).trim();
        }

        try {
            Jws<Claims> jws = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);

            if (jws.getPayload().getExpiration().before(new Date())){
                return Optional.empty();
            }

            return Optional.of(jws);
        } catch (JwtException e) {
            return Optional.empty();
        }
    }

    public Optional<AuthEntity> findOneByToken(String token) {
        Optional<Jws<Claims>> verify = this.verify(token);

        if (verify.isPresent() && ACCESS_TOKEN_TYPE.equals(verify.get().getPayload().get(TOKEN_TYPE_CLAIM, String.class))) {
            return authRepository.findOneByUUID(verify.get().getPayload().getSubject());
        } else {
            return Optional.empty();
        }
    }
    
    public String getSubject(String token) {
        return verify(token)
                .map(jws -> jws.getPayload().getSubject())
                .orElse(null);
    }

    public RefreshTokenClaims getRefreshTokenClaims(String token) {
        return verify(token)
                .filter(jws -> REFRESH_TOKEN_TYPE.equals(jws.getPayload().get(TOKEN_TYPE_CLAIM, String.class)))
                .map(jws -> new RefreshTokenClaims(
                        jws.getPayload().getSubject(),
                        jws.getPayload().getId(),
                        jws.getPayload().get(FAMILY_ID_CLAIM, String.class),
                        jws.getPayload().getExpiration().getTime()
                ))
                .orElse(null);
    }

    public long getRefreshTokenExpSeconds() {
        return refreshTokenExp / 1000L;
    }

    public record RefreshTokenClaims(String userUuid, String tokenId, String familyId, long expirationAtMillis) {
    }
}
