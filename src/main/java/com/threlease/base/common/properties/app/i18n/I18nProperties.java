package com.threlease.base.common.properties.app.i18n;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class I18nProperties {
    /**
     * false이면 Accept-Language를 무시하고 defaultLocale만 사용합니다.
     */
    private boolean enabled = true;

    /**
     * 서버 기본 언어 코드입니다. 예: ko, en
     */
    private String defaultLocale = "ko";

    /**
     * 서버가 허용하는 언어 목록입니다.
     */
    private List<String> supportedLocales = new ArrayList<>(List.of("ko", "en"));
}
