package com.threlease.base.utils.jsonwebtoken;

import com.threlease.base.entites.AuthEntity;
import com.threlease.base.repositories.AuthRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final long exp = 1000L * 60 * 60 * 8;
    private final String issuer = "auto-trades";
    private final SecretKey key = Jwts.SIG.HS512.key().build();

    private final AuthRepository authRepository;
    private final CustomUserDetailsService userDetailsService;

    public String sign(String payload) {
        return Jwts.builder()
                .subject(payload)
                .issuer(issuer)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + exp))
                .signWith(key)
                .compact();
    }

    public Optional<Jws<Claims>> verify(String token) {
        if (token == null || token.isEmpty() || !token.substring(0, "Bearer ".length()).equalsIgnoreCase("Bearer ")) {
            return Optional.empty();
        }
        token = token.split(" ")[1].trim();

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

    public boolean validateToken(String token) {
        if (token == null || token.isEmpty() || !token.substring(0, "Bearer ".length()).equalsIgnoreCase("Bearer ")) {
            return false;
        }
        token = token.split(" ")[1].trim();

        try {
            Jws<Claims> jws = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);

            return !jws.getPayload().getExpiration().before(new Date());
        } catch (JwtException e) {
            return false;
        }
    }

    public Optional<AuthEntity> findOneByToken(String token) {
        boolean success = this.validateToken(token);
        if (success || verify(token).isPresent()) {
            String uuid = verify(token).get().getPayload().toString();

            return authRepository.findOneByUUID(uuid);
        } else {
            return Optional.empty();
        }
    }

    public Authentication getAuthentication(String token) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(verify(token).isPresent() ? verify(token).get().getPayload().getSubject() : "");
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }
}
