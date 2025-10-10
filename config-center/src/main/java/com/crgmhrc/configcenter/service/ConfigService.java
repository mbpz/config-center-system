package com.crgmhrc.configcenter.service;

import com.crgmhrc.configcenter.entity.ConfigItem;
import com.crgmhrc.configcenter.mapper.ConfigItemMapper;
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
    private CacheService cacheService;

    /**
     * 获取配置（三级缓存）
     * 1. 本地缓存（Caffeine）
     * 2. Redis缓存
     * 3. MySQL数据库
     */
    public ConfigItem getConfig(String key, String environment) {
        logger.debug("开始获取配置: key={}, environment={}", key, environment);
        
        // 1. 尝试从本地缓存获取
        ConfigItem config = cacheService.getFromLocalCache(key, environment);
        if (config != null) {
            logger.debug("从本地缓存获取配置成功: key={}, environment={}", key, environment);
            return config;
        }

        // 2. 尝试从Redis获取
        config = cacheService.getFromRedis(key, environment);
        if (config != null) {
            logger.debug("从Redis缓存获取配置成功: key={}, environment={}", key, environment);
            return config;
        }

        // 3. 从数据库获取
        logger.debug("从数据库获取配置: key={}, environment={}", key, environment);
        config = configItemMapper.findByKeyAndEnvironment(key, environment);
        if (config != null) {
            // 将配置存入Redis（如果Redis可用）
            cacheService.setToRedis(key, environment, config);
            logger.debug("配置已从数据库获取并缓存: key={}, environment={}", key, environment);
        } else {
            logger.warn("配置不存在: key={}, environment={}", key, environment);
        }
        return config;
    }
    
    public List<ConfigItem> getConfigsByEnvironment(String environment) {
        logger.debug("获取环境配置列表: environment={}", environment);
        return configItemMapper.findByEnvironment(environment);
    }
    
    @Transactional
    @CacheEvict(value = "localCache", key = "#configItem.configKey + ':' + #configItem.environment")
    public void createConfig(ConfigItem configItem) {
        logger.info("创建新配置: key={}, environment={}", configItem.getConfigKey(), configItem.getEnvironment());
        configItemMapper.insert(configItem);
        // 将新配置存入Redis（如果Redis可用）
        cacheService.setToRedis(configItem.getConfigKey(), configItem.getEnvironment(), configItem);
        logger.info("配置创建成功: key={}, environment={}", configItem.getConfigKey(), configItem.getEnvironment());
    }
    
    @Transactional
    @CacheEvict(value = "localCache", key = "#configItem.configKey + ':' + #configItem.environment")
    public void updateConfig(ConfigItem configItem) {
        logger.info("更新配置: key={}, environment={}", configItem.getConfigKey(), configItem.getEnvironment());
        configItemMapper.update(configItem);
        // 更新Redis中的配置（如果Redis可用）
        cacheService.setToRedis(configItem.getConfigKey(), configItem.getEnvironment(), configItem);
        logger.info("配置更新成功: key={}, environment={}", configItem.getConfigKey(), configItem.getEnvironment());
    }
    
    @Transactional
    @CacheEvict(value = "localCache", key = "#key + ':' + #environment")
    public void deleteConfig(String key, String environment) {
        logger.info("删除配置: key={}, environment={}", key, environment);
        configItemMapper.delete(key, environment);
        // 从Redis中删除配置（如果Redis可用）
        cacheService.removeFromRedis(key, environment);
        logger.info("配置删除成功: key={}, environment={}", key, environment);
    }

    /**
     * 重新缓存指定环境的所有配置
     */
    public void refreshEnvironmentCache(String environment) {
        logger.info("开始重新缓存环境配置: environment={}", environment);
        
        // 清理指定环境的缓存
        cacheService.clearEnvironmentCache(environment);
        
        // 从数据库获取所有配置并重新缓存
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
        
        // 清理所有缓存
        cacheService.clearAllConfigCache();
        
        // 获取所有环境的配置并重新缓存
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