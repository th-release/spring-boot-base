package com.threlease.base.common.properties.crypto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("crypto")
@Getter
@Setter
public class CryptoProperties {
    private AesProperties aes;

    @Getter
    @Setter
    public static class AesProperties {
        private String secretKey;
    }
}
