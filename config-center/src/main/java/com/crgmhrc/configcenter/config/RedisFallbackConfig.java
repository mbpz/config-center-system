package com.crgmhrc.configcenter.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Redis降级配置
 * 当Redis不可用时，使用本地内存缓存作为备选方案
 */
@Configuration
@EnableCaching
@ConditionalOnProperty(name = "spring.redis.enabled", havingValue = "false")
public class RedisFallbackConfig {

    @Bean
    @Primary
    public CacheManager fallbackCacheManager() {
        return new ConcurrentMapCacheManager("configs", "default");
    }
} 