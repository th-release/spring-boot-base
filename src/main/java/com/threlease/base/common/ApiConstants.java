package com.threlease.base.common;

/**
 * API 관련 상수들을 정의하는 클래스입니다.
 */
public final class ApiConstants {

    private ApiConstants() {
        // 인스턴스화 방지
    }

    // API Base Paths
    public static final String API_PREFIX = "/api";
    public static final String VERSION_1 = "v1";

    // Auth Endpoints
    public static final String AUTH_BASE = "/auth";
    public static final String AUTH_LOGIN = "/login";
    public static final String AUTH_SIGNUP = "/signup";
    public static final String AUTH_REFRESH = "/refresh";
    public static final String AUTH_ME = "/@me";

    // Static & Public Paths
    public static final String SWAGGER_UI = "/swagger-ui/**";
    public static final String API_DOCS = "/v3/api-docs/**";
    public static final String ACTUATOR = "/actuator/**";
}
