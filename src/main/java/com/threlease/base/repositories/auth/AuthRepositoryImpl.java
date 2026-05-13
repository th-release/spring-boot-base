package com.threlease.base.repositories.auth;

import com.threlease.base.common.dto.SearchDto;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.threlease.base.entities.AuthEntity;
import com.threlease.base.entities.QAuthEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@RequiredArgsConstructor
public class AuthRepositoryImpl implements AuthRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<AuthEntity> searchUsers(SearchDto searchDto, Pageable pageable) {
        QAuthEntity auth = QAuthEntity.authEntity;
        BooleanBuilder where = new BooleanBuilder()
                .and(auth.deletedAt.isNull());

        applyKeywordFilter(where, auth, searchDto);
        applySecondaryKeywordFilter(where, auth, searchDto);
        applyDateFilter(where, auth, searchDto);

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

    private void applyKeywordFilter(BooleanBuilder where, QAuthEntity auth, SearchDto searchDto) {
        if (searchDto == null || searchDto.normalizedSearchWrd().isBlank()) {
            return;
        }

        String keyword = searchDto.normalizedSearchWrd();
        String condition = searchDto.searchConditions().getOrDefault("searchCnd1", "");

        switch (condition.toLowerCase()) {
            case "uuid" -> where.and(auth.uuid.containsIgnoreCase(keyword));
            case "username" -> where.and(auth.username.containsIgnoreCase(keyword));
            case "nickname" -> where.and(auth.nickname.containsIgnoreCase(keyword));
            case "email" -> where.and(auth.email.containsIgnoreCase(keyword));
            case "status" -> where.and(auth.status.stringValue().equalsIgnoreCase(keyword));
            case "type" -> where.and(auth.type.stringValue().equalsIgnoreCase(keyword));
            default -> where.and(auth.username.containsIgnoreCase(keyword)
                    .or(auth.nickname.containsIgnoreCase(keyword))
                    .or(auth.email.containsIgnoreCase(keyword))
                    .or(auth.uuid.containsIgnoreCase(keyword)));
        }
    }

    private void applySecondaryKeywordFilter(BooleanBuilder where, QAuthEntity auth, SearchDto searchDto) {
        if (searchDto == null || searchDto.normalizedSearchWrd2().isBlank()) {
            return;
        }

        String keyword = searchDto.normalizedSearchWrd2();
        where.and(auth.username.containsIgnoreCase(keyword)
                .or(auth.nickname.containsIgnoreCase(keyword))
                .or(auth.email.containsIgnoreCase(keyword))
                .or(auth.uuid.containsIgnoreCase(keyword)));
    }

    private void applyDateFilter(BooleanBuilder where, QAuthEntity auth, SearchDto searchDto) {
        if (searchDto == null) {
            return;
        }

        LocalDateTime startDate = parseStartDate(searchDto.normalizedSearchStartDate());
        if (startDate != null) {
            where.and(auth.createdAt.goe(startDate));
        }

        LocalDateTime endDate = parseEndDate(searchDto.normalizedSearchEndDate());
        if (endDate != null) {
            where.and(auth.createdAt.loe(endDate));
        }

        LocalDate searchDate = parseDate(searchDto.normalizedSearchDate());
        if (searchDate != null) {
            where.and(auth.createdAt.goe(searchDate.atStartOfDay())
                    .and(auth.createdAt.loe(searchDate.atTime(LocalTime.MAX))));
        }
    }

    private LocalDateTime parseStartDate(String value) {
        LocalDate date = parseDate(value);
        return date == null ? null : date.atStartOfDay();
    }

    private LocalDateTime parseEndDate(String value) {
        LocalDate date = parseDate(value);
        return date == null ? null : date.atTime(LocalTime.MAX);
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }
}
