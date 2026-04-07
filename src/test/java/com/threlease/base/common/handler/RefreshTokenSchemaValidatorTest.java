package com.threlease.base.common.handler;

import com.threlease.base.common.properties.app.token.TokenProperties;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RefreshTokenSchemaValidatorTest {

    @Test
    void validateSchemaPassesWhenRequiredColumnsExist() {
        TokenProperties properties = new TokenProperties();
        properties.setStorage("rdb");
        properties.setValidateSchema(true);

        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject("select current_schema()", String.class)).thenReturn("public");
        when(jdbcTemplate.queryForList(
                """
                select table_name
                from information_schema.tables
                where table_schema = ?
                order by table_name
                """,
                String.class,
                "public"
        ))
                .thenReturn(List.of("refresh_token_entity"));
        when(jdbcTemplate.queryForList(
                """
                select column_name
                from information_schema.columns
                where table_schema = ?
                  and lower(table_name) = lower(?)
                """,
                String.class,
                "public",
                "refresh_token_entity"
        ))
                .thenReturn(List.of(
                        "id", "userUuid", "tokenId", "familyId", "tokenHash",
                        "expiryDate", "revoked", "replacedByTokenId",
                        "createdAt", "updatedAt", "deletedAt"
                ));
        when(jdbcTemplate.queryForList(
                """
                select column_name, data_type, is_nullable
                from information_schema.columns
                where table_schema = ?
                  and lower(table_name) = lower(?)
                """,
                "public",
                "refresh_token_entity"
        ))
                .thenReturn(List.of(
                        Map.of("column_name", "id", "data_type", "bigint", "is_nullable", "NO"),
                        Map.of("column_name", "userUuid", "data_type", "character varying", "is_nullable", "NO"),
                        Map.of("column_name", "tokenId", "data_type", "character varying", "is_nullable", "NO"),
                        Map.of("column_name", "familyId", "data_type", "character varying", "is_nullable", "NO"),
                        Map.of("column_name", "tokenHash", "data_type", "character varying", "is_nullable", "NO"),
                        Map.of("column_name", "expiryDate", "data_type", "timestamp without time zone", "is_nullable", "NO"),
                        Map.of("column_name", "revoked", "data_type", "boolean", "is_nullable", "NO"),
                        Map.of("column_name", "replacedByTokenId", "data_type", "character varying", "is_nullable", "YES"),
                        Map.of("column_name", "createdAt", "data_type", "timestamp without time zone", "is_nullable", "NO"),
                        Map.of("column_name", "updatedAt", "data_type", "timestamp without time zone", "is_nullable", "YES"),
                        Map.of("column_name", "deletedAt", "data_type", "timestamp without time zone", "is_nullable", "YES")
                ));
        when(jdbcTemplate.queryForList(
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
                "public",
                "refresh_token_entity"
        ))
                .thenReturn(List.of("tokenId"));

        RefreshTokenSchemaValidator validator = new RefreshTokenSchemaValidator(properties, jdbcTemplate);

        assertDoesNotThrow(validator::validateSchema);
    }

    @Test
    void validateSchemaFailsWhenRequiredColumnsAreMissing() {
        TokenProperties properties = new TokenProperties();
        properties.setStorage("rdb");
        properties.setValidateSchema(true);

        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject("select current_schema()", String.class)).thenReturn("public");
        when(jdbcTemplate.queryForList(
                """
                select table_name
                from information_schema.tables
                where table_schema = ?
                order by table_name
                """,
                String.class,
                "public"
        ))
                .thenReturn(List.of("refresh_token_entity"));
        when(jdbcTemplate.queryForList(
                """
                select column_name
                from information_schema.columns
                where table_schema = ?
                  and lower(table_name) = lower(?)
                """,
                String.class,
                "public",
                "refresh_token_entity"
        ))
                .thenReturn(List.of("id", "userUuid"));

        RefreshTokenSchemaValidator validator = new RefreshTokenSchemaValidator(properties, jdbcTemplate);

        assertThrows(IllegalStateException.class, validator::validateSchema);
    }
}
