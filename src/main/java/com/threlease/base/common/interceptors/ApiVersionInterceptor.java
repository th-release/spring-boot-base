package com.threlease.base.common.interceptors;

import com.threlease.base.common.annotation.DeprecatedApi;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ApiVersionInterceptor implements HandlerInterceptor {
    private static final Pattern VERSION_PATTERN = Pattern.compile("/api/(v\\d+)/");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Matcher matcher = VERSION_PATTERN.matcher(request.getRequestURI() + "/");
        if (matcher.find()) {
            response.setHeader("X-API-Version", matcher.group(1));
        }

        if (handler instanceof HandlerMethod handlerMethod) {
            DeprecatedApi deprecatedApi = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), DeprecatedApi.class);
            if (deprecatedApi == null) {
                deprecatedApi = AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), DeprecatedApi.class);
            }
            if (deprecatedApi != null) {
                response.setHeader("Deprecation", "true");
                response.setHeader("X-API-Deprecated-Since", deprecatedApi.since());
                if (!deprecatedApi.sunset().isBlank()) {
                    response.setHeader("Sunset", deprecatedApi.sunset());
                }
                if (!deprecatedApi.replacement().isBlank()) {
                    response.setHeader("Link", "<" + deprecatedApi.replacement() + ">; rel=\"successor-version\"");
                }
            }
        }
        return true;
    }
}
