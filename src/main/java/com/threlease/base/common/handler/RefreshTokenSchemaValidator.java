package com.threlease.base.common.handler;

import com.threlease.base.common.properties.app.token.TokenProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

@Component
@RequiredArgsConstructor
public class RefreshTokenSchemaValidator {
    private static final String TABLE_NAME = "RefreshTokenEntity";
    private static final Set<String> REQUIRED_COLUMNS = Set.of(
            "id",
            "userUuid",
            "tokenId",
            "familyId",
            "tokenHash",
            "expiryDate",
            "revoked",
            "replacedByTokenId",
            "createdAt",
            "updatedAt",
            "deletedAt"
    );

    private final TokenProperties tokenProperties;
    private final JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void validateSchema() {
        if (!"rdb".equalsIgnoreCase(tokenProperties.getStorage())) {
            return;
        }

        String schema = jdbcTemplate.queryForObject("select current_schema()", String.class);
        String resolvedTableName = resolveTableName(schema);
        List<String> columns = jdbcTemplate.queryForList(
                """
                select column_name
                from information_schema.columns
                where table_schema = ?
                  and lower(table_name) = lower(?)
                """,
                String.class,
                schema,
                resolvedTableName
        );

        Set<String> existingColumns = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        existingColumns.addAll(columns);
        if (existingColumns.isEmpty()) {
            throw new IllegalStateException("Refresh token schema validation failed: table '" + resolvedTableName + "' exists but no columns were discovered in schema '" + schema + "'.");
        }

        Set<String> normalizedExistingColumns = new TreeSet<>();
        existingColumns.stream()
                .map(this::normalizeName)
                .forEach(normalizedExistingColumns::add);

        Set<String> missingColumns = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        REQUIRED_COLUMNS.stream()
                .filter(requiredColumn -> !normalizedExistingColumns.contains(normalizeName(requiredColumn)))
                .forEach(missingColumns::add);
        if (!missingColumns.isEmpty()) {
            throw new IllegalStateException("Refresh token schema validation failed. Missing columns in '" + resolvedTableName + "': " + String.join(", ", missingColumns));
        }
    }

    private String resolveTableName(String schema) {
        List<String> availableTables = jdbcTemplate.queryForList(
                """
                select table_name
                from information_schema.tables
                where table_schema = ?
                order by table_name
                """,
                String.class,
                schema
        );

        String normalizedTarget = normalizeName(TABLE_NAME);
        for (String tableName : availableTables) {
            if (normalizeName(tableName).equals(normalizedTarget)) {
                return tableName;
            }
        }

        throw new IllegalStateException(
                "Refresh token schema validation failed: table '" + TABLE_NAME + "' was not found in schema '" + schema + "'. "
                        + "Available tables: " + String.join(", ", availableTables)
        );
    }

    private String normalizeName(String value) {
        return value == null ? "" : value.replaceAll("[^A-Za-z0-9]", "").toLowerCase(Locale.ROOT);
    }
}
