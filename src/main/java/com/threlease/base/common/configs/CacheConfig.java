package com.threlease.base.common.configs;

import com.threlease.base.common.properties.app.redis.RedisProperties;
import lombok.RequiredArgsConstructor;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class CacheConfig {

    private final RedisProperties redisProperties;

    /**
     * app.redis.enabled=false 일 때 (기본값) Local Cache 사용
     */
    @Bean
    @Primary
    @ConditionalOnProperty(name = "app.redis.enabled", havingValue = "false", matchIfMissing = true)
    public CacheManager localCacheManager() {
        return new ConcurrentMapCacheManager();
    }

    /**
     * app.redis.enabled=true 일 때만 Redis 관련 빈 생성
     */
    @Bean
    @ConditionalOnProperty(name = "app.redis.enabled", havingValue = "true")
    public RedisConnectionFactory redisConnectionFactory() {
        String host = redisProperties.getHost() != null ? redisProperties.getHost() : "localhost";
        int port = (redisProperties.getPort() != null && !redisProperties.getPort().isEmpty()) 
                   ? Integer.parseInt(redisProperties.getPort()) : 6379;
        return new LettuceConnectionFactory(host, port);
    }

    @Bean
    @ConditionalOnProperty(name = "app.redis.enabled", havingValue = "true")
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "app.redis.enabled", havingValue = "true")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }

    @Bean
    @ConditionalOnProperty(name = "app.redis.enabled", havingValue = "true")
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    /**
     * RedissonClient 빈 생성 (분산 락 용)
     */
    @Bean
    @ConditionalOnProperty(name = "app.redis.enabled", havingValue = "true")
    public RedissonClient redissonClient() {
        String host = redisProperties.getHost() != null ? redisProperties.getHost() : "localhost";
        String port = (redisProperties.getPort() != null && !redisProperties.getPort().isEmpty()) 
                      ? redisProperties.getPort() : "6379";
        
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://" + host + ":" + port);
        
        return Redisson.create(config);
    }
}
