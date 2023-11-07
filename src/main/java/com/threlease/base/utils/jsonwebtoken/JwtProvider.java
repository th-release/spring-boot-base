package com.threlease.base.utils.jsonwebtoken;

import com.threlease.base.entites.AuthEntity;
import com.threlease.base.repositories.AuthRepository;
import lombok.RequiredArgsConstructor;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.NumericDate;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.keys.HmacKey;
import org.jose4j.lang.ByteUtil;
import org.jose4j.lang.JoseException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtProvider {
    private static final Key TOKEN_SECRET =
            new HmacKey(ByteUtil.randomBytes(64));

    private final long exp = 1000L * 60 * 60 * 8;

    private final AuthRepository authRepository;

    private final CustomUserDetailsService userDetailsService;

    public String sign(String payload) throws JoseException {
        JwtClaims claims = new JwtClaims();
        claims.setIssuedAt(NumericDate.now());
        claims.setExpirationTime(NumericDate.fromMilliseconds(System.currentTimeMillis() + exp));
        claims.setSubject(payload);

        JsonWebSignature jws = new JsonWebSignature();

        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA512);
        jws.setPayload(claims.toJson());
        jws.setKey(TOKEN_SECRET);

        return jws.getCompactSerialization();
    }

    public String verify(String token) throws JoseException, InvalidJwtException, MalformedClaimException {
        if (token == null || token.isEmpty() || !token.substring(0, "Bearer ".length()).equalsIgnoreCase("Bearer ")) {
            return "";
        }
        token = token.split(" ")[1].trim();

        JsonWebSignature jws = new JsonWebSignature();

        jws.setKey(TOKEN_SECRET);
        jws.setAlgorithmConstraints(new AlgorithmConstraints(
                AlgorithmConstraints.ConstraintType.PERMIT,
                AlgorithmIdentifiers.HMAC_SHA512));

        jws.setCompactSerialization(token);

        JwtClaims jwtClaims = JwtClaims.parse(jws.getPayload());
        NumericDate expirationTime = jwtClaims.getExpirationTime();
        NumericDate now = NumericDate.now();

        if (now.isAfter(expirationTime)) return "";
        return jwtClaims.getSubject();
    }

    public boolean validateToken(String token) throws JoseException, InvalidJwtException, MalformedClaimException {
        if (token == null || token.isEmpty() || !token.substring(0, "Bearer ".length()).equalsIgnoreCase("Bearer ")) {
            return false;
        }
        token = token.split(" ")[1].trim();

        JsonWebSignature jws = new JsonWebSignature();

        jws.setKey(TOKEN_SECRET);
        jws.setAlgorithmConstraints(new AlgorithmConstraints(
                AlgorithmConstraints.ConstraintType.PERMIT,
                AlgorithmIdentifiers.HMAC_SHA512));

        jws.setCompactSerialization(token);

        JwtClaims jwtClaims = JwtClaims.parse(jws.getPayload());
        NumericDate expirationTime = jwtClaims.getExpirationTime();
        NumericDate now = NumericDate.now();

        if (now.isAfter(expirationTime)) return false;

        return jws.isDoKeyValidation();
    }

    public Optional<AuthEntity> findByToken(String token) throws JoseException, InvalidJwtException, MalformedClaimException {
        boolean success = this.validateToken(token);
        if (success) {
            String uuid = verify(token);

            return authRepository.findOneByUUID(uuid);
        } else {
            return Optional.empty();
        }
    }

    public Authentication getAuthentication(String token) throws JoseException, InvalidJwtException, MalformedClaimException {
        UserDetails userDetails = userDetailsService.loadUserByUsername(this.verify(token));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }
}