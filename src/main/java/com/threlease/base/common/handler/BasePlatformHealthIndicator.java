package com.threlease.base.common.handler;

import com.threlease.base.common.properties.app.redis.RedisProperties;
import com.threlease.base.common.properties.storage.StorageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

@Component("basePlatform")
@RequiredArgsConstructor
public class BasePlatformHealthIndicator implements HealthIndicator {
    private final JdbcTemplate jdbcTemplate;
    private final RedisProperties redisProperties;
    private final StorageProperties storageProperties;

    @Override
    public Health health() {
        Health.Builder builder = Health.up();
        try {
            Integer database = jdbcTemplate.queryForObject("select 1", Integer.class);
            builder.withDetail("database", database != null && database == 1 ? "UP" : "UNKNOWN");
        } catch (Exception e) {
            return Health.down(e).withDetail("database", "DOWN").build();
        }

        if (Boolean.TRUE.equals(redisProperties.getEnabled())) {
            builder.withDetail("redisConfigured", true)
                    .withDetail("redisHost", redisProperties.getHost() + ":" + redisProperties.getPort());
        } else {
            builder.withDetail("redisConfigured", false);
        }

        try {
            Path storageRoot = Path.of(storageProperties.getLocal().getPath());
            builder.withDetail("storageWritable", Files.exists(storageRoot) ? Files.isWritable(storageRoot) : Files.isWritable(storageRoot.getParent()));
        } catch (Exception e) {
            builder.withDetail("storageWritable", false);
        }
        return builder.build();
    }
}
