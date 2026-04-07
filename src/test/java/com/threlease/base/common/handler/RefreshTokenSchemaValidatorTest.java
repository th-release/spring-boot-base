package com.threlease.base.common.handler;

import com.threlease.base.common.properties.app.token.TokenProperties;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RefreshTokenSchemaValidatorTest {

    @Test
    void validateSchemaPassesWhenRequiredColumnsExist() {
        TokenProperties properties = new TokenProperties();
        properties.setStorage("rdb");

        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject("select current_schema()", String.class)).thenReturn("public");
        when(jdbcTemplate.queryForList(anyString(), eq(String.class), eq("public"), eq("RefreshTokenEntity")))
                .thenReturn(List.of(
                        "id", "userUuid", "tokenId", "familyId", "tokenHash",
                        "expiryDate", "revoked", "replacedByTokenId",
                        "createdAt", "updatedAt", "deletedAt"
                ));

        RefreshTokenSchemaValidator validator = new RefreshTokenSchemaValidator(properties, jdbcTemplate);

        assertDoesNotThrow(validator::validateSchema);
    }

    @Test
    void validateSchemaFailsWhenRequiredColumnsAreMissing() {
        TokenProperties properties = new TokenProperties();
        properties.setStorage("rdb");

        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject("select current_schema()", String.class)).thenReturn("public");
        when(jdbcTemplate.queryForList(anyString(), eq(String.class), eq("public"), eq("RefreshTokenEntity")))
                .thenReturn(List.of("id", "userUuid"));

        RefreshTokenSchemaValidator validator = new RefreshTokenSchemaValidator(properties, jdbcTemplate);

        assertThrows(IllegalStateException.class, validator::validateSchema);
    }
}
