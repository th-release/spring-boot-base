package com.threlease.base.common.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/**
 * JSON 관련 유틸리티 메서드를 제공하는 정적 클래스입니다.
 */
public class JsonUtils {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Gson GSON_COMPACT = new Gson();

    private JsonUtils() {
        // 정적 유틸리티 클래스는 인스턴스화할 수 없습니다.
    }

    /**
     * 객체를 JSON 문자열로 변환합니다. (들여쓰기 적용)
     *
     * @param object 변환할 객체
     * @return JSON 형식의 문자열
     */
    public static String toJson(Object object) {
        return GSON.toJson(object);
    }

    /**
     * 객체를 압축된 JSON 문자열로 변환합니다. (들여쓰기 없음)
     *
     * @param object 변환할 객체
     * @return 압축된 JSON 형식의 문자열
     */
    public static String toJsonCompact(Object object) {
        return GSON_COMPACT.toJson(object);
    }

    /**
     * JSON 문자열을 지정된 타입의 객체로 변환합니다.
     *
     * @param json JSON 형식의 문자열
     * @param clazz 변환할 객체의 Class 타입
     * @param <T> 객체 타입
     * @return JSON 문자열로부터 생성된 객체
     * @throws JsonSyntaxException JSON 구문이 유효하지 않을 경우 발생
     */
    public static <T> T fromJson(String json, Class<T> clazz) throws JsonSyntaxException {
        return GSON_COMPACT.fromJson(json, clazz);
    }

    /**
     * JSON 문자열을 보기 좋게 들여쓰기된 형태로 변환합니다.
     *
     * @param json 압축된 JSON 문자열
     * @return 들여쓰기된 JSON 문자열
     * @throws JsonSyntaxException JSON 구문이 유효하지 않을 경우 발생
     */
    public static String prettyPrintJson(String json) throws JsonSyntaxException {
        Object jsonObject = GSON_COMPACT.fromJson(json, Object.class);
        return GSON.toJson(jsonObject);
    }
}
