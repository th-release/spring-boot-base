package com.threlease.base.common.properties.app.email;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("app.email")
@Getter
@Setter
public class EmailProperties {
    private boolean enabled = false;
    private String host;
    private int port = 587;
    private String username;
    private String password;
    private String protocol = "smtp";
    private boolean auth = true;
    private boolean starttls = true;
    private String fromAddress;
    private String fromName = "spring-boot-base";
    private long connectTimeoutMillis = 5000;
    private long readTimeoutMillis = 5000;
    private long writeTimeoutMillis = 5000;

    @PostConstruct
    public void validate() {
        if (!enabled) {
            return;
        }

        if (isBlank(host)) {
            throw new IllegalStateException("app.email.enabled=true but app.email.host is blank");
        }
        if (port <= 0) {
            throw new IllegalStateException("app.email.enabled=true but app.email.port must be greater than 0");
        }
        if (isBlank(username)) {
            throw new IllegalStateException("app.email.enabled=true but app.email.username is blank");
        }
        if (isBlank(password)) {
            throw new IllegalStateException("app.email.enabled=true but app.email.password is blank");
        }
        if (isBlank(fromAddress)) {
            throw new IllegalStateException("app.email.enabled=true but app.email.from-address is blank");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
