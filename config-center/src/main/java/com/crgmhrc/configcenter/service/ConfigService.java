/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2026 mbpz
 */

package com.crgmhrc.configcenter.service;

import com.crgmhrc.configcenter.config.EncryptionException;
import com.crgmhrc.configcenter.config.EncryptionService;
import com.crgmhrc.configcenter.entity.ConfigChangeLog;
import com.crgmhrc.configcenter.entity.ConfigItem;
import com.crgmhrc.configcenter.mapper.AuditMapper;
import com.crgmhrc.configcenter.mapper.ConfigItemMapper;
import com.crgmhrc.configcenter.security.SecurityUtils;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ConfigService {

    private static final Logger logger = LoggerFactory.getLogger(ConfigService.class);

    @Autowired
    private ConfigItemMapper configItemMapper;

    @Autowired
    private AuditMapper auditMapper;

    @Autowired
    private CacheService cacheService;

    @Autowired
    private EncryptionService encryptionService;

    @Autowired(required = false)
    private MeterRegistry meterRegistry;

    /**
     * 获取配置（三级缓存）
     * 1. 本地缓存（Caffeine）
     * 2. Redis缓存
     * 3. MySQL数据库
     */
    public ConfigItem getConfig(String key, String environment) {
        logger.debug("开始获取配置: key={}, environment={}", key, environment);

        long start = System.nanoTime();
        String cacheLayer = "miss";

        try {
            // L1: 本地缓存
            ConfigItem config = cacheService.getFromLocalCache(key, environment);
            if (config != null) {
                cacheLayer = "local";
                return config;
            }

            // L2: Redis
            config = cacheService.getFromRedis(key, environment);
            if (config != null) {
                cacheLayer = "redis";
                return config;
            }

            // L3: 数据库
            config = configItemMapper.findByKeyAndEnvironment(key, environment);
            if (config != null) {
                // 解密处理 (V2: 自动检测 provider)
                if (Boolean.TRUE.equals(config.getEncrypted()) && config.getConfigValue() != null) {
                    try {
                        String decrypted = encryptionService.decrypt(config.getConfigValue(), environment);
                        config.setConfigValue(decrypted);
                    } catch (EncryptionException e) {
                        logger.error("配置解密失败: key={}, provider={}, error={}",
                                key, encryptionService.getPrimaryProviderName(), e.getMessage());
                    }
                }
                cacheService.setToRedis(key, environment, config);
            }
            return config;
        } finally {
            recordMetrics(cacheLayer, System.nanoTime() - start);
        }
    }

    /**
     * 记录 Prometheus 指标
     */
    private void recordMetrics(String cacheLayer, long durationNanos) {
        if (meterRegistry == null) return;
        try {
            Timer.builder("config_read")
                    .tag("cache_layer", cacheLayer)
                    .register(meterRegistry)
                    .record(durationNanos, TimeUnit.NANOSECONDS);

            if ("miss".equals(cacheLayer)) {
                Counter.builder("cache_miss_total")
                        .register(meterRegistry)
                        .increment();
            } else {
                Counter.builder("cache_hit_total")
                        .tag("layer", cacheLayer)
                        .register(meterRegistry)
                        .increment();
            }
        } catch (Exception e) {
            logger.debug("指标记录失败: {}", e.getMessage());
        }
    }

    public List<ConfigItem> getConfigsByEnvironment(String environment) {
        List<ConfigItem> configs = configItemMapper.findByEnvironment(environment);
        // 列表中不解密值，只标记为已加密（前端显示 ****）
        for (ConfigItem config : configs) {
            if (Boolean.TRUE.equals(config.getEncrypted()) && config.getConfigValue() != null) {
                config.getConfigValue(); // 不做解密操作
            }
        }
        return configs;
    }

    @Transactional
    @CacheEvict(value = "localCache", key = "#configItem.configKey + ':' + #configItem.environment")
    public void createConfig(ConfigItem configItem) {
        logger.info("创建新配置: key={}, environment={}, encrypted={}",
                configItem.getConfigKey(), configItem.getEnvironment(), configItem.getEncrypted());

        // V2 加密处理: 支持自动加密 (按 key 模式) + 显式 encrypted 标记
        String plainValue = configItem.getConfigValue();
        boolean shouldEncrypt = Boolean.TRUE.equals(configItem.getEncrypted())
                || (plainValue != null && encryptionService.shouldAutoEncrypt(configItem.getConfigKey()));

        if (shouldEncrypt && plainValue != null) {
            configItem.setEncrypted(true);
            configItem.setConfigValue(encryptionService.encrypt(plainValue, configItem.getEnvironment()));
        }

        configItemMapper.insert(configItem);
        // 缓存中存储明文（解密后的值）
        ConfigItem cacheItem = copyConfigItem(configItem);
        cacheItem.setConfigValue(plainValue);
        cacheService.setToRedis(configItem.getConfigKey(), configItem.getEnvironment(), cacheItem);

        // 写入审计日志（审计中不记录明文值，标记为 ***）
        recordAudit(configItem.getConfigKey(), configItem.getEnvironment(),
                null, shouldEncrypt ? "***ENCRYPTED***" : plainValue, "CREATE");

        logger.info("配置创建成功: key={}, environment={}", configItem.getConfigKey(), configItem.getEnvironment());
    }

    @Transactional
    @CacheEvict(value = "localCache", key = "#configItem.configKey + ':' + #configItem.environment")
    public void updateConfig(ConfigItem configItem) {
        logger.info("更新配置: key={}, environment={}, encrypted={}",
                configItem.getConfigKey(), configItem.getEnvironment(), configItem.getEncrypted());

        // 获取旧值用于审计
        ConfigItem oldConfig = configItemMapper.findByKeyAndEnvironment(
                configItem.getConfigKey(), configItem.getEnvironment());
        String oldValue = oldConfig != null ? oldConfig.getConfigValue() : null;
        boolean wasEncrypted = oldConfig != null && Boolean.TRUE.equals(oldConfig.getEncrypted());

        // V2 加密处理
        String plainValue = configItem.getConfigValue();
        boolean shouldEncrypt = Boolean.TRUE.equals(configItem.getEncrypted())
                || (plainValue != null && encryptionService.shouldAutoEncrypt(configItem.getConfigKey()));

        if (shouldEncrypt && plainValue != null) {
            configItem.setEncrypted(true);
            configItem.setConfigValue(encryptionService.encrypt(plainValue, configItem.getEnvironment()));
        }

        configItemMapper.update(configItem);
        // 缓存中存储明文
        ConfigItem cacheItem = copyConfigItem(configItem);
        cacheItem.setConfigValue(plainValue);
        cacheService.setToRedis(configItem.getConfigKey(), configItem.getEnvironment(), cacheItem);

        // 写入审计日志
        recordAudit(configItem.getConfigKey(), configItem.getEnvironment(),
                wasEncrypted ? "***ENCRYPTED***" : oldValue,
                shouldEncrypt ? "***ENCRYPTED***" : plainValue,
                "UPDATE");

        logger.info("配置更新成功: key={}, environment={}", configItem.getConfigKey(), configItem.getEnvironment());
    }

    @Transactional
    @CacheEvict(value = "localCache", key = "#key + ':' + #environment")
    public void deleteConfig(String key, String environment) {
        logger.info("删除配置: key={}, environment={}", key, environment);

        // 获取旧值用于审计
        ConfigItem oldConfig = configItemMapper.findByKeyAndEnvironment(key, environment);
        String oldValue = oldConfig != null ? oldConfig.getConfigValue() : null;
        boolean wasEncrypted = oldConfig != null && Boolean.TRUE.equals(oldConfig.getEncrypted());

        configItemMapper.delete(key, environment);
        cacheService.removeFromRedis(key, environment);

        // 写入审计日志
        recordAudit(key, environment,
                wasEncrypted ? "***ENCRYPTED***" : oldValue,
                null, "DELETE");

        logger.info("配置删除成功: key={}, environment={}", key, environment);
    }

    /**
     * 复制 ConfigItem（用于缓存场景）
     */
    private ConfigItem copyConfigItem(ConfigItem source) {
        ConfigItem copy = new ConfigItem();
        copy.setId(source.getId());
        copy.setConfigKey(source.getConfigKey());
        copy.setConfigValue(source.getConfigValue());
        copy.setDescription(source.getDescription());
        copy.setEnvironment(source.getEnvironment());
        copy.setVersion(source.getVersion());
        copy.setStatus(source.getStatus());
        copy.setEncrypted(source.getEncrypted());
        copy.setCreateTime(source.getCreateTime());
        copy.setUpdateTime(source.getUpdateTime());
        return copy;
    }

    /**
     * 记录审计日志（异步降级，不阻塞主操作）
     */
    private void recordAudit(String key, String environment,
                              String oldValue, String newValue, String changeType) {
        try {
            ConfigChangeLog log = new ConfigChangeLog();
            log.setConfigKey(key);
            log.setEnvironment(environment);
            log.setOldValue(oldValue);
            log.setNewValue(newValue);
            log.setChangeType(changeType);
            log.setOperator(SecurityUtils.getCurrentUsername());
            auditMapper.insert(log);
        } catch (Exception e) {
            logger.warn("审计写入失败: key={}, error={}", key, e.getMessage());
        }
    }

    /**
     * 重新缓存指定环境的所有配置
     */
    public void refreshEnvironmentCache(String environment) {
        logger.info("开始重新缓存环境配置: environment={}", environment);
        cacheService.clearEnvironmentCache(environment);

        List<ConfigItem> configs = configItemMapper.findByEnvironment(environment);
        int cachedCount = 0;

        for (ConfigItem config : configs) {
            try {
                cacheService.setToRedis(config.getConfigKey(), config.getEnvironment(), config);
                cachedCount++;
            } catch (Exception e) {
                logger.error("重新缓存配置失败: key={}, environment={}", config.getConfigKey(), environment, e);
            }
        }

        logger.info("环境 {} 重新缓存完成，共缓存 {} 个配置", environment, cachedCount);
    }

    /**
     * 重新缓存所有环境的配置
     */
    public void refreshAllCache() {
        logger.info("开始重新缓存所有环境配置");
        cacheService.clearAllConfigCache();

        String[] environments = {"dev", "test", "prod"};
        int totalCached = 0;

        for (String environment : environments) {
            List<ConfigItem> configs = configItemMapper.findByEnvironment(environment);
            for (ConfigItem config : configs) {
                try {
                    cacheService.setToRedis(config.getConfigKey(), config.getEnvironment(), config);
                    totalCached++;
                } catch (Exception e) {
                    logger.error("重新缓存配置失败: key={}, environment={}", config.getConfigKey(), environment, e);
                }
            }
        }

        logger.info("所有环境重新缓存完成，共缓存 {} 个配置", totalCached);
    }

    /**
     * 检查Redis连接状态
     */
    public boolean isRedisAvailable() {
        return cacheService.isRedisAvailable();
    }
}
