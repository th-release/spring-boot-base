package com.threlease.base.common.utils.masking;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 다국가 기준 개인정보 마스킹 유틸
 *
 * - 이름 : CJK(한/중/일) ↔ 서양권 자동 감지 후 분기
 * - 전화번호 : libphonenumber 파싱 기반, 국가코드 포함 국제번호 지원
 * - 이메일 : local-part 앞 2자 노출, 나머지 마스킹
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MaskingComponent {

    private final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

    // =========================================================
    // 이름
    // =========================================================

    /**
     * 이름을 마스킹합니다.
     * CJK 문자(한/중/일)가 포함된 경우 동양권 규칙, 그 외 서양권 규칙 적용.
     *
     * 동양권 규칙:
     *   - 1자 : 전체 마스킹 → "*"
     *   - 2자 : 마지막 1자 마스킹 → "홍*"
     *   - 3자 이상 : 중간 마스킹 → "홍*동", "홍**길동"
     *
     * 서양권 규칙 (성 + 이름 공백 구분):
     *   - First name 첫 글자 노출, 나머지 마스킹 → "J*** Smith"
     *   - 공백 없는 단일 이름은 동양권 규칙과 동일하게 처리
     *
     * @param name 원본 이름
     * @return 마스킹된 이름 (null/blank 입력 시 그대로 반환)
     */
    public String maskName(String name) {
        if (name == null || name.isBlank()) return name;

        String trimmed = name.trim();
        return hasCjkCharacter(trimmed) ? maskCjkName(trimmed) : maskWesternName(trimmed);
    }

    private boolean hasCjkCharacter(String text) {
        return text.codePoints().anyMatch(cp ->
                Character.UnicodeBlock.of(cp) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS ||
                Character.UnicodeBlock.of(cp) == Character.UnicodeBlock.HANGUL_SYLLABLES ||
                Character.UnicodeBlock.of(cp) == Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO ||
                Character.UnicodeBlock.of(cp) == Character.UnicodeBlock.HIRAGANA ||
                Character.UnicodeBlock.of(cp) == Character.UnicodeBlock.KATAKANA
        );
    }

    private String maskCjkName(String name) {
        int len = name.length();
        if (len == 1) return "*";
        if (len == 2) return name.charAt(0) + "*";
        return name.charAt(0) + "*".repeat(len - 2) + name.charAt(len - 1);
    }

    private String maskWesternName(String name) {
        String[] parts = name.split("\\s+");
        if (parts.length == 1) return maskCjkName(name);

        // First name 첫 글자만 노출
        String maskedFirst = parts[0].charAt(0) + "*".repeat(parts[0].length() - 1);

        StringBuilder sb = new StringBuilder(maskedFirst);
        for (int i = 1; i < parts.length; i++) {
            sb.append(" ").append(parts[i]);
        }
        return sb.toString();
    }

    // =========================================================
    // 전화번호
    // =========================================================

    /**
     * 전화번호를 마스킹합니다.
     * libphonenumber 로 파싱하여 국가별 형식을 유지한 채 중간 숫자를 마스킹합니다.
     *
     * 예시:
     *   "+821012345678"  → "+82 10-****-5678"
     *   "+12025550123"   → "+1 202-***-0123"
     *   "+447911123456"  → "+44 7911 ***456"
     *
     * @param phoneNumber   원본 전화번호 (국제번호 권장: "+821012345678", 국내번호도 허용)
     * @param defaultRegion 국내번호 입력 시 기준 국가 코드 (예: "KR", "US"). 국제번호면 null 가능.
     * @return 마스킹된 전화번호 (null/blank 입력 시 그대로 반환)
     */
    public String maskPhone(String phoneNumber, String defaultRegion) {
        if (phoneNumber == null || phoneNumber.isBlank()) return phoneNumber;

        try {
            PhoneNumber parsed = phoneUtil.parse(phoneNumber, defaultRegion);
            String formatted = phoneUtil.format(parsed, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);

            String digitsOnly = formatted.replaceAll("[^0-9]", "");
            String countryCode = String.valueOf(parsed.getCountryCode());
            String subscriberDigits = digitsOnly.substring(countryCode.length());

            String maskedSubscriber = maskSubscriberNumber(subscriberDigits);
            return replaceSubscriberInFormatted(formatted, maskedSubscriber);

        } catch (NumberParseException e) {
            log.debug("전화번호 파싱 실패, fallback 마스킹 적용: {}", phoneNumber);
            return fallbackMaskPhone(phoneNumber);
        }
    }

    /** defaultRegion 없이 호출하는 단축 메서드 (국제번호 전용) */
    public String maskPhone(String phoneNumber) {
        return maskPhone(phoneNumber, null);
    }

    private String maskSubscriberNumber(String subscriber) {
        int len = subscriber.length();
        if (len <= 4) return "*".repeat(len);
        return subscriber.substring(0, 2) + "*".repeat(len - 4) + subscriber.substring(len - 2);
    }

    private String replaceSubscriberInFormatted(String formatted, String masked) {
        StringBuilder result = new StringBuilder(formatted);
        int maskIdx = masked.length() - 1;
        int fmtIdx = result.length() - 1;

        while (maskIdx >= 0 && fmtIdx >= 0) {
            if (Character.isDigit(result.charAt(fmtIdx))) {
                result.setCharAt(fmtIdx, masked.charAt(maskIdx--));
            }
            fmtIdx--;
        }
        return result.toString();
    }

    private String fallbackMaskPhone(String phone) {
        char[] chars = phone.toCharArray();
        int digits = 0;
        for (char c : chars) if (Character.isDigit(c)) digits++;

        int start = digits / 4;
        int maskCount = digits / 2;
        int current = 0;

        for (int i = 0; i < chars.length; i++) {
            if (Character.isDigit(chars[i])) {
                if (current >= start && current < start + maskCount) chars[i] = '*';
                current++;
            }
        }
        return new String(chars);
    }

    // =========================================================
    // 이메일
    // =========================================================

    /**
     * 이메일을 마스킹합니다.
     *
     * 마스킹 규칙:
     *   - local-part 가 2자 이하 : 전체 마스킹 → "**@gmail.com"
     *   - local-part 가 3자 이상 : 앞 2자 노출, 나머지 마스킹 → "ho***@gmail.com"
     *
     * @param email 원본 이메일
     * @return 마스킹된 이메일 (null/blank 또는 @ 없는 입력 시 그대로 반환)
     */
    public String maskEmail(String email) {
        if (email == null || email.isBlank()) return email;

        int atIndex = email.lastIndexOf('@');
        if (atIndex <= 0) return email;

        String local = email.substring(0, atIndex);
        String domain = email.substring(atIndex);

        if (local.length() <= 2) return "*".repeat(local.length()) + domain;
        return local.substring(0, 2) + "*".repeat(local.length() - 2) + domain;
    }
}
