package com.threlease.base.common.properties.app.database;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("app.database")
@Getter
@Setter
public class DatabaseProperties {
    private String jpaSchema = "base";
    private String flywaySchema = "base";
    private String flywayHistorySchema = "public";
}
