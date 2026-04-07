package com.threlease.base.common.handler;

import com.threlease.base.common.HttpConstants;
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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.net.URLDecoder;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.regex.Pattern;

/**
 * API 요청/응답 로깅 필터
 * 설정에 따라 Request/Response Payload 로깅 여부를 결정하며, 민감 정보를 마스킹합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoggingFilter extends OncePerRequestFilter {

    private final LoggingProperties loggingProperties;
    private static final Gson PRETTY_GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String MASK_VALUE = "********";
    private static final String MDC_KEY_CORRELATION_ID = "correlationId";
    private static final Set<String> JSON_CONTENT_TYPES = new HashSet<>();
    private static final Set<String> FORM_CONTENT_TYPES = new HashSet<>();

    static {
        JSON_CONTENT_TYPES.add("application/json");
        JSON_CONTENT_TYPES.add("application/x-json");
        JSON_CONTENT_TYPES.add("text/json");
        FORM_CONTENT_TYPES.add("application/x-www-form-urlencoded");
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();

        // /api로 시작하지 않는 요청은 로깅하지 않음
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

        // Correlation ID 생성 및 MDC/Header에 추가
        String correlationId = request.getHeader(HttpConstants.HEADER_CORRELATION_ID);
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }
        MDC.put(MDC_KEY_CORRELATION_ID, correlationId);
        response.setHeader(HttpConstants.HEADER_CORRELATION_ID, correlationId);

        HttpServletRequest requestToUse = request;
        HttpServletResponse responseToUse = response;

        boolean isRequestLoggingEnabled = loggingProperties.isRequest();
        boolean isResponseLoggingEnabled = loggingProperties.isResponse();

        if (isRequestLoggingEnabled && !(request instanceof ContentCachingRequestWrapper)) {
            requestToUse = new ContentCachingRequestWrapper(request);
        }
        if (isResponseLoggingEnabled && !(response instanceof ContentCachingResponseWrapper)) {
            responseToUse = new ContentCachingResponseWrapper(response);
        }

        long startTime = System.currentTimeMillis();

        try {
            filterChain.doFilter(requestToUse, responseToUse);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            
            if (isRequestLoggingEnabled && requestToUse instanceof ContentCachingRequestWrapper) {
                logRequest((ContentCachingRequestWrapper) requestToUse, responseToUse.getStatus(), duration);
            }
            
            if (isResponseLoggingEnabled && responseToUse instanceof ContentCachingResponseWrapper) {
                logResponse((ContentCachingResponseWrapper) responseToUse);
                ((ContentCachingResponseWrapper) responseToUse).copyBodyToResponse();
            }
            
            MDC.clear();
        }
    }

    private void logRequest(ContentCachingRequestWrapper request, int status, long duration) throws UnsupportedEncodingException {
        String queryString = request.getQueryString();
        String uri = queryString == null
                ? request.getRequestURI()
                : request.getRequestURI() + "?" + maskQueryString(queryString, loggingProperties.getSensitiveFields());
        String contentType = request.getContentType();
        String payload = getContent(request.getContentAsByteArray(), contentType, request.getCharacterEncoding());

        log.info("API Request - ID: [{}], Method: [{}], URI: [{}], Status: [{}], Duration: [{}ms], Payload: [{}]",
                MDC.get(MDC_KEY_CORRELATION_ID),
                request.getMethod(),
                uri,
                status,
                duration,
                maskPayload(payload, loggingProperties.getSensitiveFields())
        );
    }

    private void logResponse(ContentCachingResponseWrapper response) throws UnsupportedEncodingException {
        String contentType = response.getContentType();
        String payload = getContent(response.getContentAsByteArray(), contentType, response.getCharacterEncoding());
        log.info("API Response - ID: [{}], Status: [{}], Payload: [{}]",
                MDC.get(MDC_KEY_CORRELATION_ID),
                response.getStatus(),
                maskPayload(payload, loggingProperties.getSensitiveFields())
        );
    }

    private String getContent(byte[] content, String contentType, String encoding) throws UnsupportedEncodingException {
        if (content == null || content.length == 0) return "";

        String normalizedContentType = (contentType != null) ? contentType.toLowerCase().split(";")[0] : "";
        if (!JSON_CONTENT_TYPES.contains(normalizedContentType) && !FORM_CONTENT_TYPES.contains(normalizedContentType)) {
            return "[Binary or Non-JSON Content]";
        }

        String charset = (encoding != null) ? encoding : "UTF-8";
        String contentString = new String(content, charset);
        
        if (contentString.length() > loggingProperties.getMaxPayloadSize()) {
            return contentString.substring(0, loggingProperties.getMaxPayloadSize()) + "... (truncated)";
        }

        return contentString;
    }

    private String maskPayload(String payload, List<String> sensitiveFields) {
        if (payload == null || payload.isBlank() || sensitiveFields == null || sensitiveFields.isEmpty()) {
            return payload;
        }

        Set<String> normalizedSensitiveFields = normalizeSensitiveFields(sensitiveFields);

        try {
            JsonElement jsonElement = JsonParser.parseString(payload);
            maskJsonElement(jsonElement, normalizedSensitiveFields);
            return maskSensitivePatterns(PRETTY_GSON.toJson(jsonElement));
        } catch (JsonSyntaxException e) {
            return maskSensitivePatterns(maskKeyValuePayload(payload, normalizedSensitiveFields));
        }
    }

    private void maskJsonElement(JsonElement element, Set<String> sensitiveFields) {
        if (element == null) return;
        
        if (element.isJsonObject()) {
            JsonObject jsonObject = element.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                if (isSensitiveKey(entry.getKey(), sensitiveFields)) {
                    jsonObject.addProperty(entry.getKey(), MASK_VALUE);
                } else {
                    maskJsonElement(entry.getValue(), sensitiveFields);
                }
            }
        } else if (element.isJsonArray()) {
            JsonArray jsonArray = element.getAsJsonArray();
            for (JsonElement item : jsonArray) {
                maskJsonElement(item, sensitiveFields);
            }
        }
    }

    private String maskQueryString(String queryString, List<String> sensitiveFields) {
        return Arrays.stream(queryString.split("&"))
                .map(part -> maskKeyValuePart(part, normalizeSensitiveFields(sensitiveFields)))
                .collect(Collectors.joining("&"));
    }

    private String maskKeyValuePayload(String payload, Set<String> sensitiveFields) {
        return Arrays.stream(payload.split("&"))
                .map(part -> maskKeyValuePart(part, sensitiveFields))
                .collect(Collectors.joining("&"));
    }

    private String maskKeyValuePart(String part, Set<String> sensitiveFields) {
        int separatorIndex = part.indexOf('=');
        if (separatorIndex < 0) {
            return part;
        }

        String rawKey = part.substring(0, separatorIndex);
        String decodedKey = URLDecoder.decode(rawKey, StandardCharsets.UTF_8);
        if (!isSensitiveKey(decodedKey, sensitiveFields)) {
            return part;
        }

        return rawKey + "=" + MASK_VALUE;
    }

    private Set<String> normalizeSensitiveFields(List<String> sensitiveFields) {
        return sensitiveFields.stream()
                .map(this::normalizeKey)
                .collect(Collectors.toSet());
    }

    private boolean isSensitiveKey(String key, Set<String> sensitiveFields) {
        String normalizedKey = normalizeKey(key);
        return sensitiveFields.stream().anyMatch(normalizedKey::contains);
    }

    private String normalizeKey(String key) {
        return key == null ? "" : key.replaceAll("[^A-Za-z0-9]", "").toLowerCase(Locale.ROOT);
    }

    private String maskSensitivePatterns(String value) {
        String maskedValue = value;
        for (String regex : loggingProperties.getSensitiveValuePatterns()) {
            maskedValue = Pattern.compile(regex).matcher(maskedValue).replaceAll(MASK_VALUE);
        }
        return maskedValue;
    }
}
