package com.threlease.base.common.properties.app.admin;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("app.admin")
@Getter
@Setter
public class AdminProperties {
    private boolean enabled = false;
    private String username;
    private String password;
    private String nickname = "관리자";
    private String email;
}
