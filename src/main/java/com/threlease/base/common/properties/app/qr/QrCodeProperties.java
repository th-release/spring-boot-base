package com.threlease.base.common.properties.app.qr;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("app.qr")
@Getter
@Setter
public class QrCodeProperties {
    private int width;
    private int height;
    private String format;
    private String charset;
    private int margin;
}
