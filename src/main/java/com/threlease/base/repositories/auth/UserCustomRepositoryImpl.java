package com.threlease.base.repositories.auth;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.threlease.base.entites.AuthEntity;
import com.threlease.base.entites.QAuthEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@AllArgsConstructor
public class UserCustomRepositoryImpl implements UserCustomRepository {
    private final JPAQueryFactory queryFactory;

    private final QAuthEntity authEntity = QAuthEntity.authEntity;

    @Override
    public Optional<AuthEntity> findOneByUsername(String username) {
        return Optional.ofNullable(
                queryFactory
                    .selectFrom(authEntity)
                    .where(authEntity.username.eq(username))
                    .fetchOne()
        );
    }
}
