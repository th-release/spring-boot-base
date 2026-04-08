package com.threlease.base.common.properties.app.outbound;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("app.outbound")
@Getter
@Setter
public class OutboundProperties {
    private long connectTimeoutSeconds = 5;
    private long readTimeoutSeconds = 5;
    private Retry retry = new Retry();
    private Headers headers = new Headers();

    @Getter
    @Setter
    public static class Retry {
        private int maxAttempts = 3;
        private long delayMillis = 1000;
    }

    @Getter
    @Setter
    public static class Headers {
        private boolean forwardCorrelationId = true;
        private boolean addIdempotencyKey = true;
    }
}
