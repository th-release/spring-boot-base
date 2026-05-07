package com.threlease.base.common.properties.app.token;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("app.token")
@Getter
@Setter
public class TokenProperties {
    private String storage; //cache or rdb
    private int maxSessionsPerUser = 5;
    private Cleanup cleanup = new Cleanup();

    @Getter
    @Setter
    public static class Cleanup {
        private boolean enabled = true;
        private String cron = "0 45 3 * * *";
    }
}
