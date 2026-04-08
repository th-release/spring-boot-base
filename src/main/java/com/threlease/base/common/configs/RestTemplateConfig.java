package com.threlease.base.common.configs;

import com.threlease.base.common.handler.OutboundRequestInterceptor;
import com.threlease.base.common.handler.RestTemplateLoggingInterceptor;
import com.threlease.base.common.properties.app.outbound.OutboundProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * 외부 API 호출을 위한 RestTemplate 설정
 */
@Configuration
@RequiredArgsConstructor
public class RestTemplateConfig {
    private final OutboundProperties outboundProperties;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(outboundProperties.getConnectTimeoutSeconds()))
                .setReadTimeout(Duration.ofSeconds(outboundProperties.getReadTimeoutSeconds()))
                .additionalInterceptors(new OutboundRequestInterceptor(outboundProperties), new RestTemplateLoggingInterceptor())
                // 응답 본문을 여러 번 읽을 수 있도록 설정 (로깅 + 실제 사용)
                .requestFactory(() -> new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()))
                .build();
    }
}
