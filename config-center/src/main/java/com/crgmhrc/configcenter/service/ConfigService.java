/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2026 mbpz
 */

package com.crgmhrc.configcenter.service;

import com.crgmhrc.configcenter.entity.ConfigChangeLog;
import com.crgmhrc.configcenter.entity.ConfigItem;
import com.crgmhrc.configcenter.mapper.AuditMapper;
import com.crgmhrc.configcenter.mapper.ConfigItemMapper;
import com.crgmhrc.configcenter.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ConfigService {

    private static final Logger logger = LoggerFactory.getLogger(ConfigService.class);

    @Autowired
    private ConfigItemMapper configItemMapper;

    @Autowired
    private AuditMapper auditMapper;

    @Autowired
    private CacheService cacheService;

    /**
     * 获取配置（三级缓存）
     * 1. 本地缓存（Caffeine）
     * 2. Redis缓存
     * 3. MySQL数据库
     */
    public ConfigItem getConfig(String key, String environment) {
        logger.debug("开始获取配置: key={}, environment={}", key, environment);

        ConfigItem config = cacheService.getFromLocalCache(key, environment);
        if (config != null) {
            return config;
        }

        config = cacheService.getFromRedis(key, environment);
        if (config != null) {
            return config;
        }

        config = configItemMapper.findByKeyAndEnvironment(key, environment);
        if (config != null) {
            cacheService.setToRedis(key, environment, config);
        }
        return config;
    }

    public List<ConfigItem> getConfigsByEnvironment(String environment) {
        return configItemMapper.findByEnvironment(environment);
    }

    @Transactional
    @CacheEvict(value = "localCache", key = "#configItem.configKey + ':' + #configItem.environment")
    public void createConfig(ConfigItem configItem) {
        logger.info("创建新配置: key={}, environment={}", configItem.getConfigKey(), configItem.getEnvironment());
        configItemMapper.insert(configItem);
        cacheService.setToRedis(configItem.getConfigKey(), configItem.getEnvironment(), configItem);

        // 写入审计日志
        recordAudit(configItem.getConfigKey(), configItem.getEnvironment(),
                null, configItem.getConfigValue(), "CREATE");

        logger.info("配置创建成功: key={}, environment={}", configItem.getConfigKey(), configItem.getEnvironment());
    }

    @Transactional
    @CacheEvict(value = "localCache", key = "#configItem.configKey + ':' + #configItem.environment")
    public void updateConfig(ConfigItem configItem) {
        logger.info("更新配置: key={}, environment={}", configItem.getConfigKey(), configItem.getEnvironment());

        // 获取旧值用于审计
        ConfigItem oldConfig = configItemMapper.findByKeyAndEnvironment(
                configItem.getConfigKey(), configItem.getEnvironment());
        String oldValue = oldConfig != null ? oldConfig.getConfigValue() : null;

        configItemMapper.update(configItem);
        cacheService.setToRedis(configItem.getConfigKey(), configItem.getEnvironment(), configItem);

        // 写入审计日志
        recordAudit(configItem.getConfigKey(), configItem.getEnvironment(),
                oldValue, configItem.getConfigValue(), "UPDATE");

        logger.info("配置更新成功: key={}, environment={}", configItem.getConfigKey(), configItem.getEnvironment());
    }

    @Transactional
    @CacheEvict(value = "localCache", key = "#key + ':' + #environment")
    public void deleteConfig(String key, String environment) {
        logger.info("删除配置: key={}, environment={}", key, environment);

        // 获取旧值用于审计
        ConfigItem oldConfig = configItemMapper.findByKeyAndEnvironment(key, environment);
        String oldValue = oldConfig != null ? oldConfig.getConfigValue() : null;

        configItemMapper.delete(key, environment);
        cacheService.removeFromRedis(key, environment);

        // 写入审计日志
        recordAudit(key, environment, oldValue, null, "DELETE");

        logger.info("配置删除成功: key={}, environment={}", key, environment);
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
