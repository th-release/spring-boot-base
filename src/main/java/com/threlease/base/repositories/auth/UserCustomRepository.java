package com.threlease.base.repositories.auth;

import com.threlease.base.entities.AuthEntity;

import java.util.Optional;

public interface UserCustomRepository {
    Optional<AuthEntity> findOneByUsername(String username);
    Optional<AuthEntity> findOneByEmail(String email);
    Optional<AuthEntity> findOneByUsernameOrEmail(String identifier);
}
