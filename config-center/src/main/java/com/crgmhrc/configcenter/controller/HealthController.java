package com.crgmhrc.configcenter.controller;

import com.crgmhrc.configcenter.entity.CacheInfo;
import com.crgmhrc.configcenter.service.CacheService;
import com.crgmhrc.configcenter.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 健康检查控制器
 */
@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    @Autowired
    private ConfigService configService;

    @Autowired
    private CacheService cacheService;

    /**
     * 检查Redis连接状态
     */
    @GetMapping("/redis")
    public Map<String, Object> checkRedisHealth() {
        Map<String, Object> result = new HashMap<>();
        boolean redisAvailable = configService.isRedisAvailable();
        
        result.put("redis_available", redisAvailable);
        result.put("message", redisAvailable ? "Redis连接正常" : "Redis连接失败，已降级到数据库");
        result.put("timestamp", System.currentTimeMillis());
        
        return result;
    }

    /**
     * 检查整体服务状态
     */
    @GetMapping
    public Map<String, Object> checkHealth() {
        Map<String, Object> result = new HashMap<>();
        boolean redisAvailable = configService.isRedisAvailable();
        
        result.put("status", "UP");
        result.put("redis_available", redisAvailable);
        result.put("cache_strategy", redisAvailable ? "三级缓存(本地+Redis+数据库)" : "二级缓存(本地+数据库)");
        result.put("timestamp", System.currentTimeMillis());
        
        return result;
    }

    /**
     * 获取所有缓存信息列表
     */
    @GetMapping("/cache/list")
    public Map<String, Object> getAllCacheInfo() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<CacheInfo> cacheInfoList = cacheService.getAllCacheInfo();
            result.put("success", true);
            result.put("data", cacheInfoList);
            result.put("total", cacheInfoList.size());
            result.put("message", "成功获取缓存列表");
            result.put("timestamp", System.currentTimeMillis());
        } catch (Exception e) {
            result.put("success", false);
            result.put("data", null);
            result.put("total", 0);
            result.put("message", "获取缓存列表失败: " + e.getMessage());
            result.put("timestamp", System.currentTimeMillis());
        }
        
        return result;
    }

    /**
     * 获取指定环境的缓存信息列表
     */
    @GetMapping("/cache/list/{environment}")
    public Map<String, Object> getEnvironmentCacheInfo(@PathVariable String environment) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<CacheInfo> cacheInfoList = cacheService.getEnvironmentCacheInfo(environment);
            result.put("success", true);
            result.put("data", cacheInfoList);
            result.put("total", cacheInfoList.size());
            result.put("environment", environment);
            result.put("message", "成功获取环境 " + environment + " 的缓存列表");
            result.put("timestamp", System.currentTimeMillis());
        } catch (Exception e) {
            result.put("success", false);
            result.put("data", null);
            result.put("total", 0);
            result.put("environment", environment);
            result.put("message", "获取环境缓存列表失败: " + e.getMessage());
            result.put("timestamp", System.currentTimeMillis());
        }
        
        return result;
    }

    /**
     * 获取缓存统计信息
     */
    @GetMapping("/cache/stats")
    public Map<String, Object> getCacheStats() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<CacheInfo> allCacheInfo = cacheService.getAllCacheInfo();
            
            // 统计各环境的缓存数量
            Map<String, Long> environmentStats = new HashMap<>();
            long totalSize = 0;
            long totalTtl = 0;
            
            for (CacheInfo cacheInfo : allCacheInfo) {
                String env = cacheInfo.getEnvironment();
                environmentStats.put(env, environmentStats.getOrDefault(env, 0L) + 1);
                
                if (cacheInfo.getSize() != null) {
                    totalSize += cacheInfo.getSize();
                }
                if (cacheInfo.getTtl() != null && cacheInfo.getTtl() > 0) {
                    totalTtl += cacheInfo.getTtl();
                }
            }
            
            result.put("success", true);
            result.put("total_cache_count", allCacheInfo.size());
            result.put("total_cache_size", totalSize);
            result.put("average_ttl", allCacheInfo.isEmpty() ? 0 : totalTtl / allCacheInfo.size());
            result.put("environment_stats", environmentStats);
            result.put("redis_available", cacheService.isRedisAvailable());
            result.put("message", "成功获取缓存统计信息");
            result.put("timestamp", System.currentTimeMillis());
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取缓存统计信息失败: " + e.getMessage());
            result.put("timestamp", System.currentTimeMillis());
        }
        
        return result;
    }

    /**
     * 刷新指定环境的缓存
     */
    @PostMapping("/cache/refresh/{environment}")
    public Map<String, Object> refreshEnvironmentCache(@PathVariable String environment) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            configService.refreshEnvironmentCache(environment);
            result.put("success", true);
            result.put("message", "环境 " + environment + " 缓存刷新成功");
            result.put("environment", environment);
            result.put("timestamp", System.currentTimeMillis());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "缓存刷新失败: " + e.getMessage());
            result.put("environment", environment);
            result.put("timestamp", System.currentTimeMillis());
        }
        
        return result;
    }

    /**
     * 刷新所有环境的缓存
     */
    @PostMapping("/cache/refresh")
    public Map<String, Object> refreshAllCache() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            configService.refreshAllCache();
            result.put("success", true);
            result.put("message", "所有环境缓存刷新成功");
            result.put("timestamp", System.currentTimeMillis());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "缓存刷新失败: " + e.getMessage());
            result.put("timestamp", System.currentTimeMillis());
        }
        
        return result;
    }
} 