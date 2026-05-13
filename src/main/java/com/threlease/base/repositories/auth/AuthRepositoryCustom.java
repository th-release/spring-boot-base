package com.threlease.base.repositories.auth;

import com.threlease.base.common.dto.SearchDto;
import com.threlease.base.entities.AuthEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuthRepositoryCustom {
    Page<AuthEntity> searchUsers(SearchDto searchDto, Pageable pageable);
}
