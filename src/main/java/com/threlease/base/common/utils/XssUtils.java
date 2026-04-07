package com.threlease.base.common.utils;

import com.threlease.base.common.annotation.AllowHtml;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.apache.commons.text.StringEscapeUtils;

/**
 * XSS 방지를 위한 문자열 치환 유틸리티
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class XssUtils {

    /**
     * 문자열 내의 HTML 특수 문자를 치환합니다.
     * 예: <script> -> &lt;script&gt;
     */
    public static String escape(String value) {
        if (value == null) return null;
        return StringEscapeUtils.escapeHtml4(value);
    }

    /**
     * 저장형 XSS 방지를 위해 잠재적인 HTML/스크립트 구문을 제거합니다.
     */
    public static String sanitize(String value) {
        if (value == null) return null;
        return Jsoup.clean(value, Safelist.none());
    }

    public static String sanitize(String value, AllowHtml.Policy policy) {
        if (value == null) return null;
        if (policy == null || policy == AllowHtml.Policy.NONE) {
            return sanitize(value);
        }
        if (policy == AllowHtml.Policy.RELAXED) {
            return Jsoup.clean(value, Safelist.relaxed());
        }
        return Jsoup.clean(value, Safelist.basic());
    }
}
