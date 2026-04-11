package com.threlease.base.repositories.auth;

import com.threlease.base.entities.AuthEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuthRepositoryCustom {
    Page<AuthEntity> searchUsers(String query, Pageable pageable);
}
