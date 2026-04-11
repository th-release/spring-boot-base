package com.threlease.base.functions.auth;

import com.threlease.base.entities.AuthEntity;
import com.threlease.base.entities.FcmDeviceTokenEntity;
import com.threlease.base.repositories.auth.FcmDeviceTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FcmDeviceTokenServiceTest {
    private FcmDeviceTokenRepository repository;
    private FcmDeviceTokenService service;

    @BeforeEach
    void setUp() {
        repository = mock(FcmDeviceTokenRepository.class);
        service = new FcmDeviceTokenService(repository);
    }

    @Test
    void registerCreatesOrUpdatesDeviceToken() {
        AuthEntity auth = AuthEntity.builder()
                .uuid("user-1")
                .build();

        when(repository.findByDeviceToken("device-token")).thenReturn(Optional.empty());
        when(repository.save(any(FcmDeviceTokenEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FcmDeviceTokenEntity entity = service.register(auth, "device-token", null, "Mozilla/5.0 Chrome", "127.0.0.1");

        assertEquals("user-1", entity.getUser().getUuid());
        assertTrue(entity.isEnabled());
    }
}
