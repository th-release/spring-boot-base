package com.threlease.base.functions.auth;

import com.threlease.base.common.enums.AuthVerificationType;
import com.threlease.base.common.exception.BusinessException;
import com.threlease.base.common.utils.crypto.HashComponent;
import com.threlease.base.common.utils.random.RandomComponent;
import com.threlease.base.entities.AuthEntity;
import com.threlease.base.entities.AuthVerificationEntity;
import com.threlease.base.repositories.auth.AuthVerificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;

import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthVerificationServiceTest {
    private AuthVerificationRepository authVerificationRepository;
    private AuthVerificationService authVerificationService;

    @BeforeEach
    void setUp() {
        authVerificationRepository = mock(AuthVerificationRepository.class);
        authVerificationService = new AuthVerificationService(
                authVerificationRepository,
                new HashComponent(),
                new RandomComponent()
        );
    }

    @Test
    void issueCodeCreatesVerificationRecord() {
        AuthEntity auth = AuthEntity.builder()
                .uuid("user-1")
                .email("user@example.com")
                .build();

        when(authVerificationRepository.save(any(AuthVerificationEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String code = authVerificationService.issueCode(auth, AuthVerificationType.PASSWORD_RESET, auth.getEmail(), 10);

        assertNotNull(code);
    }

    @Test
    void verifyCodeFailsWhenCodeIsInvalid() {
        AuthEntity auth = AuthEntity.builder()
                .uuid("user-1")
                .build();

        AuthVerificationEntity entity = AuthVerificationEntity.builder()
                .user(auth)
                .type(AuthVerificationType.PASSWORD_RESET)
                .target("user@example.com")
                .verificationHash("other-hash")
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .verified(false)
                .build();

        when(authVerificationRepository.findLatestByUserAndTypeAndVerifiedFalse(any(AuthEntity.class), any(AuthVerificationType.class), any()))
                .thenReturn(new PageImpl<>(java.util.List.of(entity)));

        assertThrows(BusinessException.class, () -> authVerificationService.verifyCode(auth, AuthVerificationType.PASSWORD_RESET, "123456"));
    }

    @Test
    void verifyCodeSucceedsWhenIssuedCodeMatches() {
        AuthEntity auth = AuthEntity.builder()
                .uuid("user-1")
                .email("user@example.com")
                .build();

        when(authVerificationRepository.save(any(AuthVerificationEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        String code = authVerificationService.issueCode(auth, AuthVerificationType.PASSWORD_RESET, auth.getEmail(), 10);

        HashComponent hashComponent = new HashComponent();
        AuthVerificationEntity entity = AuthVerificationEntity.builder()
                .user(auth)
                .type(AuthVerificationType.PASSWORD_RESET)
                .target("user@example.com")
                .verificationHash(hashComponent.generateSHA256(code))
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .verified(false)
                .build();

        when(authVerificationRepository.findLatestByUserAndTypeAndVerifiedFalse(any(AuthEntity.class), any(AuthVerificationType.class), any()))
                .thenReturn(new PageImpl<>(java.util.List.of(entity)));
        when(authVerificationRepository.save(any(AuthVerificationEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> authVerificationService.verifyCode(auth, AuthVerificationType.PASSWORD_RESET, code));
    }
}
