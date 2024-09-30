package com.threlease.base.repositories.auth;

import com.threlease.base.entites.AuthEntity;

import java.util.Optional;

public interface UserCustomRepository {
    Optional<AuthEntity> findOneByUsername(String username);
}
