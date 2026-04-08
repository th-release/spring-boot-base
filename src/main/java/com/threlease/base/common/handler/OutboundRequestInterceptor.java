package com.threlease.base.common.handler;

import com.threlease.base.common.HttpConstants;
import com.threlease.base.common.properties.app.outbound.OutboundProperties;
import org.slf4j.MDC;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.UUID;

public class OutboundRequestInterceptor implements ClientHttpRequestInterceptor {
    private final OutboundProperties outboundProperties;

    public OutboundRequestInterceptor(OutboundProperties outboundProperties) {
        this.outboundProperties = outboundProperties;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        if (outboundProperties.getHeaders().isForwardCorrelationId()) {
            String correlationId = MDC.get("correlationId");
            if (correlationId != null && !correlationId.isBlank()) {
                request.getHeaders().set(HttpConstants.HEADER_CORRELATION_ID, correlationId);
            }
        }
        if (outboundProperties.getHeaders().isAddIdempotencyKey() && isMutation(request.getMethod())) {
            if (!request.getHeaders().containsKey("Idempotency-Key")) {
                request.getHeaders().set("Idempotency-Key", UUID.randomUUID().toString());
            }
        }
        return execution.execute(request, body);
    }

    private boolean isMutation(HttpMethod method) {
        return method == HttpMethod.POST || method == HttpMethod.PUT || method == HttpMethod.PATCH || method == HttpMethod.DELETE;
    }
}
