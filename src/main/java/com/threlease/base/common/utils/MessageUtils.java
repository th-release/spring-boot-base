package com.threlease.base.common.utils;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * 다국어 메시지 처리를 위한 정적 유틸리티
 */
@Component
public class MessageUtils {

    private static MessageSource messageSource;

    public MessageUtils(MessageSource messageSource) {
        MessageUtils.messageSource = messageSource;
    }

    /**
     * 현재 로케일에 해당하는 메시지를 반환합니다.
     */
    public static String getMessage(String code) {
        return getMessage(code, null);
    }

    /**
     * 현재 로케일에 해당하는 메시지를 반환하며, 파라미터 치환을 지원합니다.
     */
    public static String getMessage(String code, Object[] args) {
        return getMessage(code, args, LocaleContextHolder.getLocale());
    }

    /**
     * 특정 로케일에 해당하는 메시지를 반환합니다.
     */
    public static String getMessage(String code, Object[] args, Locale locale) {
        try {
            return messageSource.getMessage(code, args, locale);
        } catch (Exception e) {
            return code; // 메시지가 없을 경우 코드 자체를 반환
        }
    }
}
