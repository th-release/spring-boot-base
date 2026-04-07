package com.threlease.base.common.handler;

import com.threlease.base.common.HttpConstants;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 외부 API 호출(Outbound) 시 요청/응답 로그를 남기는 인터셉터
 */
@Slf4j
public class RestTemplateLoggingInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        String correlationId = MDC.get("correlationId");
        
        // 요청 헤더에 Correlation ID 추가 (외부 시스템에 전파)
        if (correlationId != null) {
            request.getHeaders().add(HttpConstants.HEADER_CORRELATION_ID, correlationId);
        }

        logRequest(request, body, correlationId);
        long startTime = System.currentTimeMillis();
        
        ClientHttpResponse response = execution.execute(request, body);
        
        long duration = System.currentTimeMillis() - startTime;
        logResponse(response, duration, correlationId);
        
        return response;
    }

    private void logRequest(HttpRequest request, byte[] body, String correlationId) {
        log.info("External Request - ID: [{}], Method: [{}], URI: [{}], Payload: [{}]",
                correlationId,
                request.getMethod(),
                request.getURI(),
                new String(body, StandardCharsets.UTF_8)
        );
    }

    private void logResponse(ClientHttpResponse response, long duration, String correlationId) throws IOException {
        String responseBody = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
        log.info("External Response - ID: [{}], Status: [{}], Duration: [{}ms], Payload: [{}]",
                correlationId,
                response.getStatusCode(),
                duration,
                responseBody
        );
    }
}
