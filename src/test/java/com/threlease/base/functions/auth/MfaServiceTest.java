package com.threlease.base.functions.auth;

import com.threlease.base.common.exception.BusinessException;
import com.threlease.base.common.properties.app.auth.AuthSecurityProperties;
import com.threlease.base.common.properties.app.qr.QrCodeProperties;
import com.threlease.base.common.properties.crypto.CryptoProperties;
import com.threlease.base.common.utils.QR.QRCode;
import com.threlease.base.common.utils.crypto.AesComponent;
import com.threlease.base.entities.AuthEntity;
import com.threlease.base.entities.AuthMfaEntity;
import com.threlease.base.functions.auth.dto.MfaSetupResponseDto;
import com.threlease.base.repositories.auth.AuthMfaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MfaServiceTest {
    private MfaService mfaService;
    private AuthEntity user;

    @BeforeEach
    void setUp() {
        AuthSecurityProperties properties = new AuthSecurityProperties();
        properties.getMfa().setEnabled(true);
        properties.getMfa().setIssuer("spring-boot-base");
        properties.getMfa().setAllowedWindows(1);

        CryptoProperties cryptoProperties = new CryptoProperties();
        CryptoProperties.AesProperties aesProperties = new CryptoProperties.AesProperties();
        aesProperties.setSecretKey("R3zmKTgCnAp1m0zRd5DCVmTA3GQhYPYd+iJsbk1+l9c=");
        cryptoProperties.setAes(aesProperties);

        AesComponent aesComponent = new AesComponent(cryptoProperties);
        aesComponent.init();

        QrCodeProperties qrCodeProperties = new QrCodeProperties();
        qrCodeProperties.setWidth(300);
        qrCodeProperties.setHeight(300);
        qrCodeProperties.setFormat("PNG");
        qrCodeProperties.setCharset("UTF-8");
        qrCodeProperties.setMargin(1);

        AuthMfaRepository authMfaRepository = mock(AuthMfaRepository.class);
        List<AuthMfaEntity> mfaRecords = new ArrayList<>();
        when(authMfaRepository.findActiveByUserUuid("user-1")).thenAnswer(invocation -> mfaRecords.stream().findFirst());
        when(authMfaRepository.save(any(AuthMfaEntity.class))).thenAnswer(invocation -> {
            AuthMfaEntity mfa = invocation.getArgument(0);
            if (mfaRecords.isEmpty()) {
                mfaRecords.add(mfa);
            } else {
                mfaRecords.set(0, mfa);
            }
            return mfa;
        });

        mfaService = new MfaService(properties, authMfaRepository, aesComponent, new QRCode(qrCodeProperties));

        user = AuthEntity.builder()
                .uuid("user-1")
                .username("tester")
                .nickname("tester")
                .password("encoded")
                .salt("salt")
                .type(com.threlease.base.common.enums.UserTypes.USER)
                .build();
    }

    @Test
    void setupAndCompleteEnrollmentWork() {
        MfaSetupResponseDto setup = mfaService.setup(user);

        assertNotNull(setup.getSecret());
        assertNotNull(setup.getOtpAuthUri());
        assertFalse(mfaService.isEnabled(user));

        String otpCode = generateTotp(setup.getSecret(), 30, 6);
        assertDoesNotThrow(() -> mfaService.completeEnrollment(user, otpCode));
    }

    @Test
    void invalidOtpFails() {
        MfaSetupResponseDto setup = mfaService.setup(user);
        assertNotNull(setup.getSecret());

        assertThrows(BusinessException.class, () -> mfaService.completeEnrollment(user, "000000"));
    }

    @Test
    void verifyLoginDoesNotBlockUnenrolledUserWhenGloballyEnabled() {
        MfaSetupResponseDto setup = mfaService.setup(user);
        assertNotNull(setup.getSecret());
        assertDoesNotThrow(() -> mfaService.verifyLogin(user, null));
    }

    private String generateTotp(String base32Secret, long timestep, int digits) {
        try {
            byte[] key = decodeBase32(base32Secret);
            long counter = Instant.now().getEpochSecond() / timestep;
            byte[] data = ByteBuffer.allocate(8).putLong(counter).array();
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key, "HmacSHA1"));
            byte[] hash = mac.doFinal(data);
            int offset = hash[hash.length - 1] & 0x0F;
            int binary = ((hash[offset] & 0x7F) << 24)
                    | ((hash[offset + 1] & 0xFF) << 16)
                    | ((hash[offset + 2] & 0xFF) << 8)
                    | (hash[offset + 3] & 0xFF);
            int otp = binary % (int) Math.pow(10, digits);
            return String.format("%0" + digits + "d", otp);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private byte[] decodeBase32(String value) {
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        String normalized = value.replace("=", "");
        ByteBuffer buffer = ByteBuffer.allocate(normalized.length() * 5 / 8 + 1);
        int current = 0;
        int bits = 0;
        for (char c : normalized.toCharArray()) {
            int index = alphabet.indexOf(c);
            if (index < 0) {
                continue;
            }
            current = (current << 5) | index;
            bits += 5;
            if (bits >= 8) {
                buffer.put((byte) ((current >> (bits - 8)) & 0xFF));
                bits -= 8;
            }
        }
        buffer.flip();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return bytes;
    }
}
