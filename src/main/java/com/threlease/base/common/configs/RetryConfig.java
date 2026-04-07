package com.threlease.base.common.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

/**
 * Spring Retry 기능 활성화 설정
 */
@Configuration
@EnableRetry
public class RetryConfig {
}
