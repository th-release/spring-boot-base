package com.threlease.base.common.utils.random;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 보안 난수 기반 코드 생성 유틸
 *
 * {@link SecureRandom} 을 사용하여 예측 불가능한 난수를 생성합니다.
 * {@link java.util.Random} 은 예측 가능하므로 인증·보안 용도로 절대 사용하지 마세요.
 */
@Component
@RequiredArgsConstructor
public class RandomComponent {

    private final SecureRandom secureRandom = new SecureRandom();

    private static final String DIGITS       = "0123456789";
    private static final String UPPER        = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER        = "abcdefghijklmnopqrstuvwxyz";
    private static final String ALPHANUMERIC = UPPER + LOWER + DIGITS;
    // 시각적 혼동 문자(0, O, I, l, 1) 제거한 초대코드용 문자셋
    private static final String SAFE_ALPHA   = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final String SPECIAL      = "!@#$%^&*";
    private static final String PASSWORD_POOL = UPPER + LOWER + DIGITS + SPECIAL;

    // =========================================================
    // SMS 인증번호
    // =========================================================

    /**
     * SMS 인증번호를 생성합니다 (숫자만).
     *
     * @param length 자리 수 (일반적으로 4 또는 6)
     * @return 숫자로만 이루어진 인증번호 문자열
     */
    public String generateOtp(int length) {
        return generate(DIGITS, length);
    }

    /** 6자리 OTP */
    public String generateOtp() {
        return generateOtp(6);
    }

    // =========================================================
    // 임시 비밀번호
    // =========================================================

    /**
     * 임시 비밀번호를 생성합니다.
     * 대문자·소문자·숫자·특수문자가 각각 최소 1개씩 포함됩니다.
     *
     * @param length 비밀번호 길이 (최소 4, 8 이상 권장)
     * @return 임시 비밀번호 문자열
     */
    public String generateTempPassword(int length) {
        if (length < 4) throw new IllegalArgumentException("임시 비밀번호 길이는 최소 4 이상이어야 합니다.");

        char[] password = new char[length];
        password[0] = randomChar(UPPER);
        password[1] = randomChar(LOWER);
        password[2] = randomChar(DIGITS);
        password[3] = randomChar(SPECIAL);

        for (int i = 4; i < length; i++) {
            password[i] = randomChar(PASSWORD_POOL);
        }

        shuffle(password);
        return new String(password);
    }

    /** 12자리 임시 비밀번호 */
    public String generateTempPassword() {
        return generateTempPassword(12);
    }

    // =========================================================
    // 초대코드 / 쿠폰코드
    // =========================================================

    /**
     * 초대코드를 생성합니다.
     * 시각적으로 혼동되는 문자(0, O, I, l, 1)를 제외한 대문자+숫자 조합입니다.
     *
     * @param length    코드 길이
     * @param separator 구분자 삽입 간격 (0이면 구분자 없음, 예: 4 → "XXXX-XXXX")
     * @param groupChar 구분자 문자 (예: '-')
     * @return 초대코드 문자열
     */
    public String generateInviteCode(int length, int separator, char groupChar) {
        String raw = generate(SAFE_ALPHA, length);

        if (separator <= 0 || length <= separator) return raw;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < raw.length(); i++) {
            if (i > 0 && i % separator == 0) sb.append(groupChar);
            sb.append(raw.charAt(i));
        }
        return sb.toString();
    }

    /** 구분자 없는 초대코드 */
    public String generateInviteCode(int length) {
        return generateInviteCode(length, 0, '-');
    }

    /** 8자리 초대코드 (XXXX-XXXX 형식) */
    public String generateInviteCode() {
        return generateInviteCode(8, 4, '-');
    }

    // =========================================================
    // 범용 코드 생성
    // =========================================================

    /**
     * 지정한 문자셋과 길이로 난수 문자열을 생성합니다.
     *
     * @param charset 사용할 문자셋
     * @param length  생성할 길이
     * @return 난수 문자열
     */
    public String generate(String charset, int length) {
        if (charset == null || charset.isEmpty()) throw new IllegalArgumentException("문자셋이 비어 있습니다.");
        if (length <= 0) throw new IllegalArgumentException("길이는 1 이상이어야 합니다.");

        return IntStream.range(0, length)
                .map(i -> secureRandom.nextInt(charset.length()))
                .mapToObj(i -> String.valueOf(charset.charAt(i)))
                .collect(Collectors.joining());
    }

    /** 숫자+영문 혼합 코드 생성 */
    public String generateAlphanumeric(int length) {
        return generate(ALPHANUMERIC, length);
    }

    // =========================================================
    // 내부 유틸
    // =========================================================

    private char randomChar(String charset) {
        return charset.charAt(secureRandom.nextInt(charset.length()));
    }

    private void shuffle(char[] array) {
        for (int i = array.length - 1; i > 0; i--) {
            int j = secureRandom.nextInt(i + 1);
            char tmp = array[i];
            array[i] = array[j];
            array[j] = tmp;
        }
    }
}
