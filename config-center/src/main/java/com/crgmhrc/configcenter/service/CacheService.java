package com.crgmhrc.configcenter.service;

import com.crgmhrc.configcenter.entity.CacheInfo;
import com.crgmhrc.configcenter.entity.ConfigItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class CacheService {

    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    private static final String REDIS_KEY_PREFIX = "config:";
    private static final long REDIS_EXPIRE_TIME = 1; // 1小时

    /**
     * 从本地缓存获取配置
     */
    @Cacheable(value = "localCache", key = "#key + ':' + #environment", unless = "#result == null")
    public ConfigItem getFromLocalCache(String key, String environment) {
        return getFromRedis(key, environment);
    }

    /**
     * 从Redis获取配置，如果Redis不可用则返回null
     */
    public ConfigItem getFromRedis(String key, String environment) {
        if (redisTemplate == null) {
            logger.warn("Redis不可用，跳过Redis缓存读取");
            return null;
        }

        try {
            String redisKey = REDIS_KEY_PREFIX + key + ":" + environment;
            ConfigItem config = (ConfigItem) redisTemplate.opsForValue().get(redisKey);
            if (config != null) {
                logger.debug("从Redis获取配置成功: {}", redisKey);
                return config;
            }
            logger.debug("Redis中未找到配置: {}", redisKey);
            return null;
        } catch (RedisConnectionFailureException e) {
            logger.warn("Redis连接失败，跳过Redis缓存读取: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error("Redis操作异常: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 将配置存入Redis，如果Redis不可用则忽略
     */
    public void setToRedis(String key, String environment, ConfigItem config) {
        if (redisTemplate == null) {
            logger.warn("Redis不可用，跳过Redis缓存写入");
            return;
        }

        try {
            String redisKey = REDIS_KEY_PREFIX + key + ":" + environment;
            redisTemplate.opsForValue().set(redisKey, config, REDIS_EXPIRE_TIME, TimeUnit.HOURS);
            logger.debug("配置已存入Redis: {}", redisKey);
        } catch (RedisConnectionFailureException e) {
            logger.warn("Redis连接失败，跳过Redis缓存写入: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Redis写入异常: {}", e.getMessage(), e);
        }
    }

    /**
     * 从Redis删除配置，如果Redis不可用则忽略
     */
    public void removeFromRedis(String key, String environment) {
        if (redisTemplate == null) {
            logger.warn("Redis不可用，跳过Redis缓存删除");
            return;
        }

        try {
            String redisKey = REDIS_KEY_PREFIX + key + ":" + environment;
            redisTemplate.delete(redisKey);
            logger.debug("配置已从Redis删除: {}", redisKey);
        } catch (RedisConnectionFailureException e) {
            logger.warn("Redis连接失败，跳过Redis缓存删除: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Redis删除异常: {}", e.getMessage(), e);
        }
    }

    /**
     * 清理所有配置缓存
     */
    public void clearAllConfigCache() {
        if (redisTemplate == null) {
            logger.warn("Redis不可用，跳过缓存清理");
            return;
        }

        try {
            // 删除所有以config:开头的键
            Set<String> keys = redisTemplate.keys(REDIS_KEY_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                logger.info("已清理 {} 个配置缓存", keys.size());
            } else {
                logger.info("没有找到需要清理的配置缓存");
            }
        } catch (Exception e) {
            logger.error("清理缓存异常: {}", e.getMessage(), e);
        }
    }

    /**
     * 清理指定环境的配置缓存
     */
    public void clearEnvironmentCache(String environment) {
        if (redisTemplate == null) {
            logger.warn("Redis不可用，跳过缓存清理");
            return;
        }

        try {
            // 删除指定环境的所有配置缓存
            Set<String> keys = redisTemplate.keys(REDIS_KEY_PREFIX + "*:" + environment);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                logger.info("已清理环境 {} 的 {} 个配置缓存", environment, keys.size());
            } else {
                logger.info("环境 {} 没有找到需要清理的配置缓存", environment);
            }
        } catch (Exception e) {
            logger.error("清理环境缓存异常: {}", e.getMessage(), e);
        }
    }

    /**
     * 获取所有缓存信息列表
     */
    public List<CacheInfo> getAllCacheInfo() {
        List<CacheInfo> cacheInfoList = new ArrayList<>();
        
        if (redisTemplate == null) {
            logger.warn("Redis不可用，无法获取缓存信息");
            return cacheInfoList;
        }

        try {
            // 获取所有以config:开头的键
            Set<String> keys = redisTemplate.keys(REDIS_KEY_PREFIX + "*");
            if (keys == null || keys.isEmpty()) {
                logger.info("Redis中没有找到配置缓存");
                return cacheInfoList;
            }

            for (String key : keys) {
                try {
                    // 获取缓存值
                    ConfigItem config = (ConfigItem) redisTemplate.opsForValue().get(key);
                    if (config != null) {
                        // 获取TTL
                        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
                        
                        // 估算缓存大小（简单估算）
                        Long size = estimateCacheSize(config);
                        
                        // 解析环境信息
                        String environment = extractEnvironmentFromKey(key);
                        String configKey = extractConfigKeyFromKey(key);
                        
                        CacheInfo cacheInfo = new CacheInfo(
                            key,
                            environment,
                            configKey,
                            config.getConfigValue(),
                            config.getDescription(),
                            config.getVersion(),
                            config.getStatus(),
                            config.getCreateTime(),
                            config.getUpdateTime(),
                            ttl,
                            size
                        );
                        
                        cacheInfoList.add(cacheInfo);
                    }
                } catch (Exception e) {
                    logger.warn("处理缓存键 {} 时发生异常: {}", key, e.getMessage());
                }
            }
            
            logger.info("成功获取 {} 个缓存信息", cacheInfoList.size());
            return cacheInfoList;
            
        } catch (Exception e) {
            logger.error("获取缓存信息异常: {}", e.getMessage(), e);
            return cacheInfoList;
        }
    }

    /**
     * 获取指定环境的缓存信息列表
     */
    public List<CacheInfo> getEnvironmentCacheInfo(String environment) {
        List<CacheInfo> cacheInfoList = new ArrayList<>();
        
        if (redisTemplate == null) {
            logger.warn("Redis不可用，无法获取缓存信息");
            return cacheInfoList;
        }

        try {
            // 获取指定环境的所有配置缓存键
            Set<String> keys = redisTemplate.keys(REDIS_KEY_PREFIX + "*:" + environment);
            if (keys == null || keys.isEmpty()) {
                logger.info("环境 {} 中没有找到配置缓存", environment);
                return cacheInfoList;
            }

            for (String key : keys) {
                try {
                    // 获取缓存值
                    ConfigItem config = (ConfigItem) redisTemplate.opsForValue().get(key);
                    if (config != null) {
                        // 获取TTL
                        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
                        
                        // 估算缓存大小
                        Long size = estimateCacheSize(config);
                        
                        // 解析配置键
                        String configKey = extractConfigKeyFromKey(key);
                        
                        CacheInfo cacheInfo = new CacheInfo(
                            key,
                            environment,
                            configKey,
                            config.getConfigValue(),
                            config.getDescription(),
                            config.getVersion(),
                            config.getStatus(),
                            config.getCreateTime(),
                            config.getUpdateTime(),
                            ttl,
                            size
                        );
                        
                        cacheInfoList.add(cacheInfo);
                    }
                } catch (Exception e) {
                    logger.warn("处理缓存键 {} 时发生异常: {}", key, e.getMessage());
                }
            }
            
            logger.info("成功获取环境 {} 的 {} 个缓存信息", environment, cacheInfoList.size());
            return cacheInfoList;
            
        } catch (Exception e) {
            logger.error("获取环境缓存信息异常: {}", e.getMessage(), e);
            return cacheInfoList;
        }
    }

    /**
     * 检查Redis是否可用
     */
    public boolean isRedisAvailable() {
        if (redisTemplate == null) {
            return false;
        }

        try {
            redisTemplate.opsForValue().get("health_check");
            return true;
        } catch (Exception e) {
            logger.warn("Redis健康检查失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 从Redis键中提取环境信息
     */
    private String extractEnvironmentFromKey(String key) {
        if (key == null || !key.contains(":")) {
            return "unknown";
        }
        String[] parts = key.split(":");
        if (parts.length >= 3) {
            return parts[parts.length - 1]; // 最后一个部分通常是环境
        }
        return "unknown";
    }

    /**
     * 从Redis键中提取配置键
     */
    private String extractConfigKeyFromKey(String key) {
        if (key == null || !key.contains(":")) {
            return "unknown";
        }
        String[] parts = key.split(":");
        if (parts.length >= 3) {
            // 从第二个部分开始到倒数第二个部分，用:连接
            StringBuilder configKey = new StringBuilder();
            for (int i = 1; i < parts.length - 1; i++) {
                if (i > 1) {
                    configKey.append(":");
                }
                configKey.append(parts[i]);
            }
            return configKey.toString();
        }
        return "unknown";
    }

    /**
     * 估算缓存大小（字节）
     */
    private Long estimateCacheSize(ConfigItem config) {
        if (config == null) {
            return 0L;
        }
        
        long size = 0;
        size += config.getConfigKey() != null ? config.getConfigKey().getBytes().length : 0;
        size += config.getConfigValue() != null ? config.getConfigValue().getBytes().length : 0;
        size += config.getDescription() != null ? config.getDescription().getBytes().length : 0;
        size += config.getEnvironment() != null ? config.getEnvironment().getBytes().length : 0;
        size += config.getVersion() != null ? config.getVersion().getBytes().length : 0;
        size += config.getStatus() != null ? config.getStatus().getBytes().length : 0;
        
        // 时间字段的估算
        size += 24; // LocalDateTime大约24字节
        
        return size;
    }
} 