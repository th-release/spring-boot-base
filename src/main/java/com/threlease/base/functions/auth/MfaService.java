package com.threlease.base.functions.auth;

import com.threlease.base.common.exception.BusinessException;
import com.threlease.base.common.exception.ErrorCode;
import com.threlease.base.common.properties.app.auth.AuthSecurityProperties;
import com.threlease.base.common.utils.QR.QRCode;
import com.threlease.base.common.utils.crypto.AesComponent;
import com.threlease.base.entities.AuthEntity;
import com.threlease.base.entities.AuthMfaEntity;
import com.threlease.base.functions.auth.dto.MfaSetupResponseDto;
import com.threlease.base.repositories.auth.AuthMfaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class MfaService {
    private static final String BASE32_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

    private final AuthSecurityProperties authSecurityProperties;
    private final AuthMfaRepository authMfaRepository;
    private final AesComponent aesComponent;
    private final QRCode qrCode;

    public boolean isMfaGloballyEnabled() {
        return authSecurityProperties.getMfa().isEnabled();
    }

    public MfaSetupResponseDto setup(AuthEntity user) {
        assertMfaEnabled();
        String secret = resolveOrCreateSecret(user);

        String issuer = authSecurityProperties.getMfa().getIssuer();
        String otpAuthUri = "otpauth://totp/" + issuer + ":" + user.getUsername()
                + "?secret=" + secret
                + "&issuer=" + issuer
                + "&digits=" + authSecurityProperties.getMfa().getCodeDigits()
                + "&period=" + authSecurityProperties.getMfa().getTimeStepSeconds();

        try {
            return MfaSetupResponseDto.builder()
                    .secret(secret)
                    .otpAuthUri(otpAuthUri)
                    .qrCodeBase64(qrCode.generateQrCodeBase64(otpAuthUri))
                    .build();
        } catch (Exception e) {
            return MfaSetupResponseDto.builder()
                    .secret(secret)
                    .otpAuthUri(otpAuthUri)
                    .qrCodeBase64(null)
                    .build();
        }
    }

    public void completeEnrollment(AuthEntity user, String otpCode) {
        assertMfaEnabled();
        validateOtp(user, otpCode);
        AuthMfaEntity mfa = resolveOrCreateMfa(user);
        mfa.setEnabled(true);
        authMfaRepository.save(mfa);
    }

    public void reset(AuthEntity user) {
        AuthMfaEntity mfa = resolveOrCreateMfa(user);
        mfa.setEnabled(false);
        mfa.setSecret(null);
        authMfaRepository.save(mfa);
    }

    public void verifyLogin(AuthEntity user, String otpCode) {
        if (!isRequired(user)) {
            return;
        }
        validateOtp(user, otpCode);
    }

    public boolean isRequired(AuthEntity user) {
        if (!authSecurityProperties.getMfa().isEnabled()) {
            return false;
        }
        if (isEnabled(user)) {
            return true;
        }
        return false;
    }

    public boolean isEnrollmentRequired(AuthEntity user) {
        return authSecurityProperties.getMfa().isEnabled() && !isEnabled(user);
    }

    public boolean isEnabled(AuthEntity user) {
        return authMfaRepository.findActiveByUserUuid(user.getUuid())
                .map(AuthMfaEntity::isEnabled)
                .orElse(false);
    }

    private void validateOtp(AuthEntity user, String otpCode) {
        if (otpCode == null || otpCode.isBlank()) {
            throw new BusinessException(ErrorCode.MFA_REQUIRED);
        }

        String encryptedSecret = authMfaRepository.findActiveByUserUuid(user.getUuid())
                .map(AuthMfaEntity::getSecret)
                .orElse(null);
        if (encryptedSecret == null || encryptedSecret.isBlank()) {
            throw new BusinessException(ErrorCode.MFA_NOT_CONFIGURED);
        }

        String secret = aesComponent.decrypt(encryptedSecret);
        if (!isValidTotp(secret, otpCode)) {
            throw new BusinessException(ErrorCode.MFA_INVALID_CODE);
        }
    }

    private boolean isValidTotp(String base32Secret, String otpCode) {
        long timestep = authSecurityProperties.getMfa().getTimeStepSeconds();
        int allowedWindows = authSecurityProperties.getMfa().getAllowedWindows();
        long currentCounter = Instant.now().getEpochSecond() / timestep;
        for (int offset = -allowedWindows; offset <= allowedWindows; offset++) {
            if (generateTotp(base32Secret, currentCounter + offset).equals(otpCode)) {
                return true;
            }
        }
        return false;
    }

    private String generateTotp(String base32Secret, long counter) {
        try {
            byte[] key = decodeBase32(base32Secret);
            byte[] data = ByteBuffer.allocate(8).putLong(counter).array();
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key, "HmacSHA1"));
            byte[] hash = mac.doFinal(data);
            int offset = hash[hash.length - 1] & 0x0F;
            int binary = ((hash[offset] & 0x7F) << 24)
                    | ((hash[offset + 1] & 0xFF) << 16)
                    | ((hash[offset + 2] & 0xFF) << 8)
                    | (hash[offset + 3] & 0xFF);
            int digits = authSecurityProperties.getMfa().getCodeDigits();
            int otp = binary % (int) Math.pow(10, digits);
            return String.format("%0" + digits + "d", otp);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.MFA_INVALID_CODE);
        }
    }

    private String generateBase32Secret() {
        byte[] randomBytes = Base64.getDecoder().decode(Base64.getEncoder().encodeToString(java.security.SecureRandom.getSeed(20)));
        return encodeBase32(randomBytes);
    }

    private String resolveOrCreateSecret(AuthEntity user) {
        AuthMfaEntity mfa = resolveOrCreateMfa(user);
        if (mfa.getSecret() != null && !mfa.getSecret().isBlank()) {
            return aesComponent.decrypt(mfa.getSecret());
        }
        String secret = generateBase32Secret();
        mfa.setSecret(aesComponent.encrypt(secret));
        mfa.setEnabled(false);
        authMfaRepository.save(mfa);
        return secret;
    }

    private AuthMfaEntity resolveOrCreateMfa(AuthEntity user) {
        return authMfaRepository.findActiveByUserUuid(user.getUuid())
                .orElseGet(() -> AuthMfaEntity.builder()
                        .user(user)
                        .enabled(false)
                        .build());
    }

    private String encodeBase32(byte[] data) {
        StringBuilder result = new StringBuilder();
        int current = 0;
        int bits = 0;
        for (byte value : data) {
            current = (current << 8) | (value & 0xFF);
            bits += 8;
            while (bits >= 5) {
                result.append(BASE32_ALPHABET.charAt((current >> (bits - 5)) & 31));
                bits -= 5;
            }
        }
        if (bits > 0) {
            result.append(BASE32_ALPHABET.charAt((current << (5 - bits)) & 31));
        }
        return result.toString();
    }

    private byte[] decodeBase32(String value) {
        String normalized = value.replace("=", "").toUpperCase(Locale.ROOT);
        ByteBuffer buffer = ByteBuffer.allocate(normalized.length() * 5 / 8 + 1);
        int current = 0;
        int bits = 0;
        for (char c : normalized.toCharArray()) {
            int index = BASE32_ALPHABET.indexOf(c);
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

    private void assertMfaEnabled() {
        if (!authSecurityProperties.getMfa().isEnabled()) {
            throw new BusinessException(ErrorCode.MFA_DISABLED);
        }
    }
}
