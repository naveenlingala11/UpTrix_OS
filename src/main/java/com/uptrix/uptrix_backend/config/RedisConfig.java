package com.uptrix.uptrix_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis configuration using ONLY non-deprecated serializers.
 *
 * Uses RedisSerializer.json() → new recommended JSON serializer in Spring Data Redis 4.x
 */
@Configuration
public class RedisConfig {

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        // Auto-picks configuration from application.yml (spring.redis.host, port, etc.)
        return new LettuceConnectionFactory();
    }

    @Bean
    public RedisCacheManager cacheManager(LettuceConnectionFactory connectionFactory) {

        // 🔵 Key serializer (safe, readable)
        RedisSerializer<String> keySerializer = new StringRedisSerializer();

        // 🔵 Non-deprecated JSON serializer (Spring's recommended)
        RedisSerializer<Object> valueSerializer = RedisSerializer.json();

        // --------------------------
        // DEFAULT CACHE CONFIG
        // --------------------------
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(keySerializer))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer))
                .entryTtl(Duration.ofHours(1)); // default TTL

        // --------------------------
        // PER-CACHE TTL CONFIG
        // --------------------------
        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        cacheConfigs.put("employee", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigs.put("departments", defaultConfig.entryTtl(Duration.ofHours(6)));
        cacheConfigs.put("roles", defaultConfig.entryTtl(Duration.ofHours(12)));
        cacheConfigs.put("shiftMapping", defaultConfig.entryTtl(Duration.ofMinutes(60)));
        cacheConfigs.put("unreadNotifications", defaultConfig.entryTtl(Duration.ofSeconds(60)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .transactionAware()
                .build();
    }
}
