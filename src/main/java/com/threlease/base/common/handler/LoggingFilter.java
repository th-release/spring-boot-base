package com.threlease.base.common.handler;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
public class LoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put("requestId", requestId);

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();

        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logRequest(requestWrapper, duration);
            logResponse(responseWrapper);
            responseWrapper.copyBodyToResponse();
            MDC.clear();
        }
    }

    private void logRequest(ContentCachingRequestWrapper request, long duration) {
        String queryString = request.getQueryString();
        log.info("Request: [{}], Method: [{}], URI: [{}], Duration: [{}ms], Payload: [{}]",
                MDC.get("requestId"),
                request.getMethod(),
                queryString == null ? request.getRequestURI() : request.getRequestURI() + "?" + queryString,
                duration,
                getContent(request.getContentAsByteArray())
        );
    }

    private void logResponse(ContentCachingResponseWrapper response) {
        log.info("Response: [{}], Status: [{}], Payload: [{}]",
                MDC.get("requestId"),
                response.getStatus(),
                getContent(response.getContentAsByteArray())
        );
    }

    private String getContent(byte[] content) {
        if (content == null || content.length == 0) {
            return "";
        }
        return new String(content);
    }
}
