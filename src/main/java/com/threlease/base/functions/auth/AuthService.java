package com.threlease.base.functions.auth;

import com.threlease.base.entites.AuthEntity;
import com.threlease.base.repositories.AuthRepository;
import com.threlease.base.utils.jsonwebtoken.JwtProvider;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Service
public class AuthService {
    private final AuthRepository authRepository;
    private final JwtProvider jwtProvider;

    public AuthService(AuthRepository authRepository, JwtProvider jwtProvider) {
        this.authRepository = authRepository;
        this.jwtProvider = jwtProvider;
    }

    public Optional<AuthEntity> findOneByUUID(String uuid) {
        return authRepository.findOneByUUID(uuid);
    }
    public Optional<AuthEntity> findOneByUsername(String username) {
        return authRepository.findOneByUsername(username);
    }
    public void authSave(AuthEntity auth) {
        authRepository.save(auth);
    }

    public String sign(AuthEntity user) {
        return jwtProvider.sign(user.getUuid());
    }

    public Optional<AuthEntity> findOneByToken(String token) {
        return jwtProvider.findOneByToken(token);
    }
}
