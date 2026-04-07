package com.threlease.base.common;

/**
 * HTTP 관련 상수들을 정의하는 클래스입니다.
 */
public final class HttpConstants {

    private HttpConstants() {
        // 인스턴스화 방지
    }

    // Header Names
    public static final String HEADER_CORRELATION_ID = "X-Correlation-ID";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_REFRESH_TOKEN = "Refresh-Token";

    // Media Types
    public static final String APPLICATION_JSON_VALUE = "application/json";

    // Status Messages (예시)
    public static final String MSG_SUCCESS = "Success";
    public static final String MSG_INTERNAL_SERVER_ERROR = "Internal Server Error";
}
