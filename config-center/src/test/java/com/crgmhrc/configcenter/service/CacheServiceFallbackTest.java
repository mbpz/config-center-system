/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2026 mbpz
 */

package com.crgmhrc.configcenter.service;

import com.crgmhrc.configcenter.entity.ConfigItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * CacheService 降级测试 - 验证 Redis 不可用时的优雅降级行为
 */
class CacheServiceFallbackTest {

    private CacheService cacheService;
    private RedisTemplate<String, Object> redisTemplate;

    @BeforeEach
    void setUp() {
        cacheService = new CacheService();
        redisTemplate = mock(RedisTemplate.class);
        org.springframework.test.util.ReflectionTestUtils.setField(cacheService, "redisTemplate", redisTemplate);
    }

    @Test
    void getFromRedis_whenRedisUnavailable_returnsNull() {
        // redisTemplate == null 模拟 Redis 不可用
        org.springframework.test.util.ReflectionTestUtils.setField(cacheService, "redisTemplate", null);

        ConfigItem result = cacheService.getFromRedis("key", "dev");

        assertNull(result);
    }

    @Test
    void getFromRedis_whenConnectionFails_returnsNull() {
        ValueOperations<String, Object> ops = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(ops);
        when(ops.get("config:key:dev")).thenThrow(new RedisConnectionFailureException("Connection refused"));

        ConfigItem result = cacheService.getFromRedis("key", "dev");

        assertNull(result);
    }

    @Test
    void setToRedis_whenRedisUnavailable_silentlyIgnores() {
        org.springframework.test.util.ReflectionTestUtils.setField(cacheService, "redisTemplate", null);

        ConfigItem item = new ConfigItem();
        // 不应抛出任何异常
        assertDoesNotThrow(() -> cacheService.setToRedis("key", "dev", item));
    }

    @Test
    void setToRedis_whenConnectionFails_silentlyIgnores() {
        ValueOperations<String, Object> ops = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(ops);
        doThrow(new RedisConnectionFailureException("timeout"))
                .when(ops).set(anyString(), any(), anyLong(), any());

        ConfigItem item = new ConfigItem();
        assertDoesNotThrow(() -> cacheService.setToRedis("key", "dev", item));
    }

    @Test
    void removeFromRedis_whenRedisUnavailable_silentlyIgnores() {
        org.springframework.test.util.ReflectionTestUtils.setField(cacheService, "redisTemplate", null);

        assertDoesNotThrow(() -> cacheService.removeFromRedis("key", "dev"));
    }

    @Test
    void removeFromRedis_whenConnectionFails_silentlyIgnores() {
        when(redisTemplate.delete(anyString()))
                .thenThrow(new RedisConnectionFailureException("broken pipe"));

        assertDoesNotThrow(() -> cacheService.removeFromRedis("key", "dev"));
    }

    @Test
    void clearAllConfigCache_whenRedisUnavailable_silentlyIgnores() {
        org.springframework.test.util.ReflectionTestUtils.setField(cacheService, "redisTemplate", null);

        assertDoesNotThrow(() -> cacheService.clearAllConfigCache());
    }

    @Test
    void clearEnvironmentCache_whenRedisUnavailable_silentlyIgnores() {
        org.springframework.test.util.ReflectionTestUtils.setField(cacheService, "redisTemplate", null);

        assertDoesNotThrow(() -> cacheService.clearEnvironmentCache("dev"));
    }

    @Test
    void isRedisAvailable_whenRedisUnavailable_returnsFalse() {
        org.springframework.test.util.ReflectionTestUtils.setField(cacheService, "redisTemplate", null);

        assertFalse(cacheService.isRedisAvailable());
    }

    @Test
    void isRedisAvailable_whenRedisThrowsException_returnsFalse() {
        ValueOperations<String, Object> ops = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(ops);
        when(ops.get("health_check")).thenThrow(new RedisConnectionFailureException("down"));

        assertFalse(cacheService.isRedisAvailable());
    }

    @Test
    void getFromRedis_whenKeyExists_returnsValue() {
        ValueOperations<String, Object> ops = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(ops);

        ConfigItem expected = new ConfigItem();
        expected.setConfigKey("app.name");
        expected.setConfigValue("my-app");
        when(ops.get("config:app.name:dev")).thenReturn(expected);

        ConfigItem result = cacheService.getFromRedis("app.name", "dev");

        assertNotNull(result);
        assertEquals("app.name", result.getConfigKey());
        assertEquals("my-app", result.getConfigValue());
    }

    @Test
    void getFromRedis_whenKeyNotExists_returnsNull() {
        ValueOperations<String, Object> ops = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(ops);
        when(ops.get("config:missing:dev")).thenReturn(null);

        ConfigItem result = cacheService.getFromRedis("missing", "dev");

        assertNull(result);
    }

    @Test
    void setToRedis_whenRedisAvailable_succeeds() {
        ValueOperations<String, Object> ops = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(ops);

        ConfigItem item = new ConfigItem();
        item.setConfigKey("key");
        item.setConfigValue("value");

        assertDoesNotThrow(() -> cacheService.setToRedis("key", "dev", item));
        verify(ops).set(eq("config:key:dev"), eq(item), eq(1L), any());
    }

    @Test
    void isRedisAvailable_whenRedisResponds_returnsTrue() {
        ValueOperations<String, Object> ops = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(ops);
        when(ops.get("health_check")).thenReturn("OK");

        assertTrue(cacheService.isRedisAvailable());
    }

    @Test
    void clearAllConfigCache_whenKeysExist_deletesThem() {
        @SuppressWarnings("unchecked")
        java.util.Set<String> keys = new java.util.HashSet<>(Arrays.asList("config:k1:dev", "config:k2:prod"));
        when(redisTemplate.keys("config:*")).thenReturn(keys);

        cacheService.clearAllConfigCache();

        verify(redisTemplate).delete(keys);
    }

    @Test
    void clearAllConfigCache_whenNoKeys_doesNothing() {
        when(redisTemplate.keys("config:*")).thenReturn(null);

        cacheService.clearAllConfigCache();

        verify(redisTemplate, never()).delete(any(java.util.Collection.class));
    }

    @Test
    void clearEnvironmentCache_whenKeysExist_deletesThem() {
        @SuppressWarnings("unchecked")
        java.util.Set<String> keys = new java.util.HashSet<>(Arrays.asList("config:k1:dev"));
        when(redisTemplate.keys("config:*:dev")).thenReturn(keys);

        cacheService.clearEnvironmentCache("dev");

        verify(redisTemplate).delete(keys);
    }

    @Test
    void removeFromRedis_whenRedisAvailable_deletesKey() {
        cacheService.removeFromRedis("key", "dev");
        verify(redisTemplate).delete("config:key:dev");
    }

    @Test
    void fullDegradationScenario_readWriteDeleteAllSucceedWithoutRedis() {
        // 模拟 Redis 完全不可用的完整 CRUD 流程
        org.springframework.test.util.ReflectionTestUtils.setField(cacheService, "redisTemplate", null);

        ConfigItem item = new ConfigItem();
        item.setConfigKey("test.key");
        item.setConfigValue("test-value");

        // 读取返回 null（降级到数据库）
        assertNull(cacheService.getFromRedis("test.key", "dev"));

        // 写入静默忽略
        assertDoesNotThrow(() -> cacheService.setToRedis("test.key", "dev", item));

        // 删除静默忽略
        assertDoesNotThrow(() -> cacheService.removeFromRedis("test.key", "dev"));

        // 清理静默忽略
        assertDoesNotThrow(() -> cacheService.clearAllConfigCache());
    }
}
