package com.threlease.base.common.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
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
}
