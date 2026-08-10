package com.threlease.base.functions.auth;

import com.threlease.base.entities.AuthEntity;
import com.threlease.base.entities.FcmDeviceTokenEntity;
import com.threlease.base.repositories.auth.FcmDeviceTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FcmDeviceTokenServiceTest {
    private FcmDeviceTokenRepository repository;
    private JdbcTemplate jdbcTemplate;
    private FcmDeviceTokenService service;

    @BeforeEach
    void setUp() {
        repository = mock(FcmDeviceTokenRepository.class);
        jdbcTemplate = mock(JdbcTemplate.class);
        service = new FcmDeviceTokenService(repository, jdbcTemplate);
    }

    @Test
    void registerCreatesOrUpdatesDeviceToken() {
        AuthEntity auth = AuthEntity.builder()
                .uuid("user-1")
                .build();

        when(repository.findLatestActiveByDeviceToken(any(String.class), any())).thenReturn(new PageImpl<>(java.util.List.of()));
        when(repository.save(any(FcmDeviceTokenEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jdbcTemplate.queryForObject(eq("select pg_try_advisory_xact_lock(hashtext(?))"), eq(Boolean.class), eq("device-token"))).thenReturn(true);

        FcmDeviceTokenEntity entity = service.register(auth, "device-token", null, "Mozilla/5.0 Chrome", "127.0.0.1");

        assertEquals("user-1", entity.getUser().getUuid());
        assertTrue(entity.isEnabled());
        verify(jdbcTemplate).queryForObject(eq("select pg_try_advisory_xact_lock(hashtext(?))"), eq(Boolean.class), eq("device-token"));
    }
}
