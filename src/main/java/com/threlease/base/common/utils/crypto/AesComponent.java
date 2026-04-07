package com.threlease.base.common.utils.crypto;

import com.threlease.base.common.exception.CryptoException;
import com.threlease.base.common.properties.crypto.CryptoProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-256-GCM 기반 양방향 암호화 유틸
 *
 * 키 설정:
 * - application.yml에 crypto.aes.secret 가 있으면 해당 키 사용
 * - 없으면 서버 시작 시 랜덤 키 자동 생성 (재시작 시 기존 데이터 복호화 불가 — 개발 환경 전용)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AesComponent {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;    // GCM 권장 IV 길이
    private static final int TAG_LENGTH = 128;  // 인증 태그 비트

    private final CryptoProperties cryptoProperties;

    private SecretKey secretKey;

    public static String generateBase64Key() {
        byte[] key = new byte[32]; // 256 bit
        new SecureRandom().nextBytes(key);
        return Base64.getEncoder().encodeToString(key);
    }

    @PostConstruct
    public void init() {
        String base64Key = cryptoProperties.getAes() != null ? cryptoProperties.getAes().getSecretKey() : null;

        if (base64Key == null || base64Key.isBlank()) {
            System.out.println("=== AES-256 Secret Key ===");
            System.out.println(generateBase64Key());

            throw new IllegalStateException(
                    "[CryptoUtils] crypto.aes.secret-key 가 설정되지 않았습니다. " +
                            "AesKeyGenerator.main() 을 실행하여 키를 생성한 뒤 application.yml 에 등록하세요."
            );
        }

        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        if (keyBytes.length != 32) {
            throw new IllegalStateException(
                    "[CryptoUtils] AES-256 키는 32 bytes(256 bit)여야 합니다. 현재: " + keyBytes.length + " bytes"
            );
        }

        this.secretKey = new SecretKeySpec(keyBytes, "AES");
        log.info("[CryptoUtils] AES-256 키 초기화 완료");
    }

    /**
     * 평문을 AES-256-GCM으로 암호화합니다.
     * @param plainText 암호화할 평문 (null 입력 시 null 반환)
     */
    public String encrypt(String plainText) {
        if (plainText == null) return null;

        try {
            byte[] iv = generateIv();
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH, iv));

            byte[] cipherText = cipher.doFinal(plainText.getBytes());

            byte[] combined = ByteBuffer.allocate(iv.length + cipherText.length)
                    .put(iv)
                    .put(cipherText)
                    .array();

            return Base64.getEncoder().encodeToString(combined);

        } catch (Exception e) {
            throw new CryptoException("암호화 실패", e);
        }
    }

    /**
     * 암호화된 문자열을 복호화합니다.
     * @param encryptedText Base64 인코딩된 암호문 (null 입력 시 null 반환)
     */
    public String decrypt(String encryptedText) {
        if (encryptedText == null) return null;

        try {
            byte[] combined = Base64.getDecoder().decode(encryptedText);

            ByteBuffer buffer = ByteBuffer.wrap(combined);
            byte[] iv = new byte[IV_LENGTH];
            buffer.get(iv);

            byte[] cipherText = new byte[buffer.remaining()];
            buffer.get(cipherText);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH, iv));

            return new String(cipher.doFinal(cipherText));

        } catch (Exception e) {
            throw new CryptoException("복호화 실패", e);
        }
    }

    /**
     * 암호문과 평문이 동일한지 비교합니다.
     */
    public boolean matches(String plainText, String encryptedText) {
        if (plainText == null || encryptedText == null) return false;
        return plainText.equals(decrypt(encryptedText));
    }

    private byte[] generateIv() {
        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        return iv;
    }
}
