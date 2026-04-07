package com.threlease.base.common.configs;

import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

/**
 * Jackson의 JSON 직렬화/역직렬화 시 XSS 방지를 위한 설정
 */
@Configuration
public class JacksonConfig {

    @Bean
    public MappingJackson2HttpMessageConverter jsonEscapeConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.getFactory().setCharacterEscapes(new HtmlCharacterEscapes());
        return new MappingJackson2HttpMessageConverter(objectMapper);
    }

    /**
     * 특수 문자를 HTML Entity로 치환하는 내부 클래스
     */
    public static class HtmlCharacterEscapes extends CharacterEscapes {
        private final int[] asciiEscapes;

        public HtmlCharacterEscapes() {
            // 1. 기본 ASCII 이스케이프 설정
            asciiEscapes = CharacterEscapes.standardAsciiEscapesForJSON();
            // 2. XSS 취약 문자에 대해 추가 커스텀 설정
            asciiEscapes['<'] = CharacterEscapes.ESCAPE_CUSTOM;
            asciiEscapes['>'] = CharacterEscapes.ESCAPE_CUSTOM;
            asciiEscapes['&'] = CharacterEscapes.ESCAPE_CUSTOM;
            asciiEscapes['\"'] = CharacterEscapes.ESCAPE_CUSTOM;
            asciiEscapes['('] = CharacterEscapes.ESCAPE_CUSTOM;
            asciiEscapes[')'] = CharacterEscapes.ESCAPE_CUSTOM;
            asciiEscapes['#'] = CharacterEscapes.ESCAPE_CUSTOM;
            asciiEscapes['\''] = CharacterEscapes.ESCAPE_CUSTOM;
        }

        @Override
        public int[] getEscapeCodesForAscii() {
            return asciiEscapes;
        }

        @Override
        public SerializableString getEscapeSequence(int ch) {
            return new SerializedString(StringEscapeUtils.escapeHtml4(Character.toString((char) ch)));
        }
    }
}
