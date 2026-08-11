package com.threlease.base.common.configs;

import java.util.List;

public final class ApiAccessRules {
    private static final List<String> PUBLIC_API_PREFIXES = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/signup",
            "/api/v1/auth/refresh",
            "/api/v1/auth/password/reset/request",
            "/api/v1/auth/password/reset/confirm",
            "/api/v1/files/content/",
            "/api/common/enums",
            "/api/swagger",
            "/api/v3/api-docs",
            "/api/swagger-ui/",
            "/api/v3/api-docs/",
            "/api/actuator/prometheus",
            "/api/actuator/metrics",
            "/api/actuator/health"
    );

    private ApiAccessRules() {
    }

    public static boolean isPublicRequest(String method, String uri) {
        if (uri == null || method == null) {
            return false;
        }

        boolean nonApiGet = ("GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method)) && !uri.startsWith("/api/");
        if (nonApiGet) {
            return true;
        }

        if (!uri.startsWith("/api/")) {
            return false;
        }

        for (String prefix : PUBLIC_API_PREFIXES) {
            if (uri.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}
