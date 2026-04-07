package com.threlease.base.common.handler;

import com.threlease.base.common.properties.app.logging.LoggingProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * API 요청/응답 로깅 필터
 * 설정에 따라 Request/Response Payload 로깅 여부를 결정합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoggingFilter extends OncePerRequestFilter {

    private final LoggingProperties loggingProperties;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();

        // /api로 시작하지 않는 요청(Static Resource, View 등)은 로깅하지 않음
        if (!uri.startsWith("/api")) {
            return true;
        }

        List<String> excludeUrls = loggingProperties.getExcludeUrls();
        if (excludeUrls == null) return false;
        
        return excludeUrls.stream()
                .anyMatch(uri::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put("requestId", requestId);

        // 로깅 설정에 따라 Wrapper 적용 여부 결정
        HttpServletRequest requestToUse = loggingProperties.isRequest() ? new ContentCachingRequestWrapper(request) : request;
        HttpServletResponse responseToUse = loggingProperties.isResponse() ? new ContentCachingResponseWrapper(response) : response;

        long startTime = System.currentTimeMillis();

        try {
            filterChain.doFilter(requestToUse, responseToUse);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            
            if (loggingProperties.isRequest() && requestToUse instanceof ContentCachingRequestWrapper) {
                logRequest((ContentCachingRequestWrapper) requestToUse, responseToUse.getStatus(), duration);
            }
            
            if (loggingProperties.isResponse() && responseToUse instanceof ContentCachingResponseWrapper) {
                logResponse((ContentCachingResponseWrapper) responseToUse);
                ((ContentCachingResponseWrapper) responseToUse).copyBodyToResponse();
            }
            
            MDC.clear();
        }
    }

    private void logRequest(ContentCachingRequestWrapper request, int status, long duration) {
        String queryString = request.getQueryString();
        String uri = queryString == null ? request.getRequestURI() : request.getRequestURI() + "?" + queryString;
        String payload = getContent(request.getContentAsByteArray());

        log.info("API Request - ID: [{}], Method: [{}], URI: [{}], Status: [{}], Duration: [{}ms], Payload: [{}]",
                MDC.get("requestId"),
                request.getMethod(),
                uri,
                status,
                duration,
                payload
        );
    }

    private void logResponse(ContentCachingResponseWrapper response) {
        String payload = getContent(response.getContentAsByteArray());
        log.info("API Response - ID: [{}], Status: [{}], Payload: [{}]",
                MDC.get("requestId"),
                response.getStatus(),
                payload
        );
    }

    private String getContent(byte[] content) {
        if (content == null || content.length == 0) {
            return "";
        }
        return new String(content);
    }
}
