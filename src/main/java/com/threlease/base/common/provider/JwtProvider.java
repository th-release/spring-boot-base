package com.threlease.base.common.provider;

import com.threlease.base.entities.AuthEntity;
import com.threlease.base.repositories.auth.AuthRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final long accessTokenExp = 1000L * 60 * 60; // 1 hour
    private final long refreshTokenExp = 1000L * 60 * 60 * 24 * 14; // 14 days
    private final String issuer = "spring-boot-base";
    private final SecretKey key = Jwts.SIG.HS512.key().build();

    private final AuthRepository authRepository;

    public String createAccessToken(String uuid) {
        return createToken(uuid, accessTokenExp);
    }

    public String createRefreshToken(String uuid) {
        return createToken(uuid, refreshTokenExp);
    }

    private String createToken(String payload, long expiration) {
        return Jwts.builder()
                .subject(payload)
                .issuer(issuer)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
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
                    .verifyWith(key)
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

        if (verify.isPresent()) {
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
}
