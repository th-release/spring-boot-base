package com.threlease.base.functions.auth;

import com.threlease.base.entities.AuthEntity;
import com.threlease.base.repositories.auth.AuthRepository;
import com.threlease.base.common.provider.JwtProvider;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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

    @Cacheable(value = "user", key = "#uuid", unless = "#result == null")
    public Optional<AuthEntity> findOneByUUID(String uuid) {
        return authRepository.findOneByUUID(uuid);
    }

    @Cacheable(value = "user", key = "#username", unless = "#result == null")
    public Optional<AuthEntity> findOneByUsername(String username) {
        return authRepository.findOneByUsername(username);
    }

    @Caching(evict = {
            @CacheEvict(value = "user", key = "#auth.uuid"),
            @CacheEvict(value = "user", key = "#auth.username")
    })
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
