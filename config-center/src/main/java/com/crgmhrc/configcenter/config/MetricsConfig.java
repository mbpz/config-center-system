/**
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2026 mbpz
 */
package com.crgmhrc.configcenter.config;

import com.crgmhrc.configcenter.service.ConfigService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 自定义 Prometheus 指标配置
 */
@Configuration
public class MetricsConfig {

    /**
     * 缓存命中计数器
     */
    @Bean
    public Counter cacheHitCounter(MeterRegistry registry) {
        return Counter.builder("cache_hit_total")
                .description("Total cache hits by layer")
                .tag("type", "unknown")
                .register(registry);
    }

    /**
     * 缓存未命中计数器
     */
    @Bean
    public Counter cacheMissCounter(MeterRegistry registry) {
        return Counter.builder("cache_miss_total")
                .description("Total cache misses")
                .register(registry);
    }

    /**
     * Redis 可用性指标
     */
    @Bean
    public Gauge redisAvailableGauge(MeterRegistry registry, ConfigService configService) {
        return Gauge.builder("redis_available", configService,
                cs -> cs.isRedisAvailable() ? 1.0 : 0.0)
                .description("Redis availability (1=available, 0=unavailable)")
                .register(registry);
    }
}
