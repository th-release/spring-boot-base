package com.threlease.base.repositories.auth;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.threlease.base.entities.AuthEntity;
import com.threlease.base.entities.QAuthEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RequiredArgsConstructor
public class AuthRepositoryImpl implements AuthRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<AuthEntity> searchUsers(String query, Pageable pageable) {
        QAuthEntity auth = QAuthEntity.authEntity;
        BooleanBuilder where = new BooleanBuilder()
                .and(auth.deletedAt.isNull());

        if (query != null && !query.isBlank()) {
            String normalizedQuery = query.trim();
            where.and(auth.username.containsIgnoreCase(normalizedQuery)
                    .or(auth.nickname.containsIgnoreCase(normalizedQuery))
                    .or(auth.email.containsIgnoreCase(normalizedQuery)));
        }

        List<AuthEntity> content = queryFactory
                .selectFrom(auth)
                .where(where)
                .orderBy(auth.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(auth.count())
                .from(auth)
                .where(where)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }
}
