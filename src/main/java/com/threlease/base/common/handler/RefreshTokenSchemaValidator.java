package com.threlease.base.common.handler;

import com.threlease.base.common.properties.app.database.DatabaseProperties;
import com.threlease.base.common.properties.app.token.TokenProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@Component
@RequiredArgsConstructor
public class RefreshTokenSchemaValidator {
    private static final String TABLE_NAME = "RefreshTokenEntity";
    private static final String EXPECTED_TOKEN_ID_COLUMN = "tokenId";
    private static final Set<String> NULLABLE_COLUMNS = Set.of("replacedByTokenId", "updatedAt", "deletedAt");
    private static final Set<String> REQUIRED_COLUMNS = Set.of(
            "id",
            "userUuid",
            "tokenId",
            "familyId",
            "tokenHash",
            "expiryDate",
            "userAgent",
            "deviceLabel",
            "ipAddress",
            "lastUsedAt",
            "revoked",
            "replacedByTokenId",
            "createdAt",
            "updatedAt",
            "deletedAt"
    );
    private static final Map<String, Set<String>> EXPECTED_COLUMN_TYPES = createExpectedColumnTypes();

    private final DatabaseProperties databaseProperties;
    private final TokenProperties tokenProperties;
    private final JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void validateSchema() {
        if (!"rdb".equalsIgnoreCase(tokenProperties.getStorage()) || !tokenProperties.isValidateSchema()) {
            return;
        }

        String schema = databaseProperties.getJpaSchema();
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

        validateColumnMetadata(schema, resolvedTableName);
        validateTokenIdUniqueConstraint(schema, resolvedTableName);
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

    private void validateColumnMetadata(String schema, String tableName) {
        List<Map<String, Object>> metadata = jdbcTemplate.queryForList(
                """
                select column_name, data_type, is_nullable
                from information_schema.columns
                where table_schema = ?
                  and lower(table_name) = lower(?)
                """,
                schema,
                tableName
        );

        Map<String, Map<String, Object>> metadataByColumn = new HashMap<>();
        for (Map<String, Object> row : metadata) {
            metadataByColumn.put(normalizeName((String) row.get("column_name")), row);
        }

        for (Map.Entry<String, Set<String>> expected : EXPECTED_COLUMN_TYPES.entrySet()) {
            String normalizedColumn = normalizeName(expected.getKey());
            Map<String, Object> row = metadataByColumn.get(normalizedColumn);
            if (row == null) {
                continue;
            }

            String actualType = String.valueOf(row.get("data_type")).toLowerCase(Locale.ROOT);
            if (!expected.getValue().contains(actualType)) {
                throw new IllegalStateException(
                        "Refresh token schema validation failed. Column '" + expected.getKey()
                                + "' in '" + tableName + "' has unexpected type '" + actualType
                                + "'. Expected one of: " + String.join(", ", expected.getValue())
                );
            }

            String nullable = String.valueOf(row.get("is_nullable"));
            if (!"NO".equalsIgnoreCase(nullable) && !NULLABLE_COLUMNS.contains(expected.getKey())) {
                throw new IllegalStateException(
                        "Refresh token schema validation failed. Column '" + expected.getKey()
                                + "' in '" + tableName + "' must be NOT NULL."
                );
            }
        }
    }

    private void validateTokenIdUniqueConstraint(String schema, String tableName) {
        List<String> constrainedColumns = jdbcTemplate.queryForList(
                """
                select kcu.column_name
                from information_schema.table_constraints tc
                join information_schema.key_column_usage kcu
                  on tc.constraint_name = kcu.constraint_name
                 and tc.table_schema = kcu.table_schema
                 and tc.table_name = kcu.table_name
                where tc.table_schema = ?
                  and lower(tc.table_name) = lower(?)
                  and tc.constraint_type = 'UNIQUE'
                """,
                String.class,
                schema,
                tableName
        );

        boolean tokenIdUnique = constrainedColumns.stream()
                .map(this::normalizeName)
                .anyMatch(normalizeName(EXPECTED_TOKEN_ID_COLUMN)::equals);

        if (!tokenIdUnique) {
            throw new IllegalStateException(
                    "Refresh token schema validation failed. Column '" + EXPECTED_TOKEN_ID_COLUMN
                            + "' in '" + tableName + "' must have a UNIQUE constraint."
            );
        }
    }

    private static Map<String, Set<String>> createExpectedColumnTypes() {
        Map<String, Set<String>> types = new HashMap<>();
        types.put("id", Set.of("bigint", "integer"));
        types.put("userUuid", Set.of("character varying", "varchar", "text"));
        types.put("tokenId", Set.of("character varying", "varchar", "text"));
        types.put("familyId", Set.of("character varying", "varchar", "text"));
        types.put("tokenHash", Set.of("character varying", "varchar", "text"));
        types.put("expiryDate", Set.of("timestamp without time zone", "timestamp with time zone"));
        types.put("userAgent", Set.of("character varying", "varchar", "text"));
        types.put("deviceLabel", Set.of("character varying", "varchar", "text"));
        types.put("ipAddress", Set.of("character varying", "varchar", "text"));
        types.put("lastUsedAt", Set.of("timestamp without time zone", "timestamp with time zone"));
        types.put("revoked", Set.of("boolean"));
        types.put("replacedByTokenId", Set.of("character varying", "varchar", "text"));
        types.put("createdAt", Set.of("timestamp without time zone", "timestamp with time zone"));
        types.put("updatedAt", Set.of("timestamp without time zone", "timestamp with time zone"));
        types.put("deletedAt", Set.of("timestamp without time zone", "timestamp with time zone"));
        return types;
    }
}
