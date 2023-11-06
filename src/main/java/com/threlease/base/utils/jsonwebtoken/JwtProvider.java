package com.threlease.base.utils.jsonwebtoken;

import com.threlease.base.entites.AuthEntity;
import com.threlease.base.repositories.AuthRepository;
import lombok.RequiredArgsConstructor;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.keys.HmacKey;
import org.jose4j.lang.ByteUtil;
import org.jose4j.lang.JoseException;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtProvider {
    private static final Key TOKEN_SECRET =
            new HmacKey(ByteUtil.randomBytes(64));

    private final AuthRepository authRepository;

    public String sign (String payload) throws JoseException {
        JsonWebSignature jws = new JsonWebSignature();

        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA512);
        jws.setPayload(payload);
        jws.setKey(TOKEN_SECRET);

        return jws.getCompactSerialization();
    }

    public String verify (String token) throws JoseException {
        if (!token.substring(0, "Bearer ".length()).equalsIgnoreCase("Bearer ")) {
            return "";
        }
        token = token.split(" ")[1].trim();

        JsonWebSignature jws = new JsonWebSignature();

        jws.setKey(TOKEN_SECRET);
        jws.setAlgorithmConstraints(new AlgorithmConstraints(
                AlgorithmConstraints.ConstraintType.PERMIT,
                AlgorithmIdentifiers.HMAC_SHA512));

        jws.setCompactSerialization(token);

        return jws.getPayload();
    }

    public boolean validateToken(String token) throws JoseException {
        if (!token.substring(0, "Bearer ".length()).equalsIgnoreCase("Bearer ")) {
            return false;
        }
        token = token.split(" ")[1].trim();

        JsonWebSignature jws = new JsonWebSignature();

        jws.setKey(TOKEN_SECRET);
        jws.setAlgorithmConstraints(new AlgorithmConstraints(
                AlgorithmConstraints.ConstraintType.PERMIT,
                AlgorithmIdentifiers.HMAC_SHA512));

        jws.setCompactSerialization(token);

        return jws.isDoKeyValidation();
    }

    public Optional<AuthEntity> findByToken(String token) throws JoseException {
        boolean success = this.validateToken(token);
        if (success) {
            String uuid = verify(token);

            return authRepository.findOneByUUID(uuid);
        } else {
            return Optional.empty();
        }
    }
}