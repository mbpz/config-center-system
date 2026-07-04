/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2026 mbpz
 */

package com.crgmhrc.configcenter.service;

import com.crgmhrc.configcenter.config.EncryptionService;
import com.crgmhrc.configcenter.entity.ConfigItem;
import com.crgmhrc.configcenter.mapper.AuditMapper;
import com.crgmhrc.configcenter.mapper.ConfigItemMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ConfigService 单元测试（不依赖 Spring 容器）
 */
class ConfigServiceTest {

    private ConfigService configService;
    private ConfigItemMapper configItemMapper;
    private AuditMapper auditMapper;
    private CacheService cacheService;
    private EncryptionService encryptionService;

    @BeforeEach
    void setUp() {
        configService = new ConfigService();
        configItemMapper = mock(ConfigItemMapper.class);
        auditMapper = mock(AuditMapper.class);
        cacheService = mock(CacheService.class);
        encryptionService = mock(EncryptionService.class);

        ReflectionTestUtils.setField(configService, "configItemMapper", configItemMapper);
        ReflectionTestUtils.setField(configService, "auditMapper", auditMapper);
        ReflectionTestUtils.setField(configService, "cacheService", cacheService);
        ReflectionTestUtils.setField(configService, "encryptionService", encryptionService);
    }

    @Test
    void createConfig_withUnencrypted_savesPlainValue() {
        ConfigItem item = createConfigItem("app.name", "my-app", false);
        // encrypted=false 时加密服务不应被调用

        configService.createConfig(item);

        verify(configItemMapper).insert(item);
        verify(cacheService).setToRedis("app.name", "dev", item);
        verify(auditMapper).insert(any());
        // 加密服务不应被调用
        verify(encryptionService, never()).encrypt(any(), any());
    }

    @Test
    void createConfig_withEncrypted_encryptsValue() {
        ConfigItem item = createConfigItem("db.password", "secret123", true);
        when(encryptionService.encrypt("secret123", "dev")).thenReturn("ENCRYPTED_SECRET");

        configService.createConfig(item);

        // 验证 insert 的是加密值
        ArgumentCaptor<ConfigItem> captor = ArgumentCaptor.forClass(ConfigItem.class);
        verify(configItemMapper).insert(captor.capture());
        assertEquals("ENCRYPTED_SECRET", captor.getValue().getConfigValue());

        // 验证缓存存入明文
        ArgumentCaptor<ConfigItem> cacheCaptor = ArgumentCaptor.forClass(ConfigItem.class);
        verify(cacheService).setToRedis(eq("db.password"), eq("dev"), cacheCaptor.capture());
        assertEquals("secret123", cacheCaptor.getValue().getConfigValue());
    }

    @Test
    void updateConfig_retrievesOldValueForAudit() {
        ConfigItem oldItem = createConfigItem("app.name", "old-value", false);
        ConfigItem newItem = createConfigItem("app.name", "new-value", false);
        when(configItemMapper.findByKeyAndEnvironment("app.name", "dev")).thenReturn(oldItem);

        configService.updateConfig(newItem);

        verify(configItemMapper).findByKeyAndEnvironment("app.name", "dev");
        verify(configItemMapper).update(any());
        verify(auditMapper).insert(any());
    }

    @Test
    void updateConfig_withEncrypted_oldValueMarkedEncrypted() {
        ConfigItem oldItem = createConfigItem("db.password", "ENCRYPTED_OLD", true);
        ConfigItem newItem = createConfigItem("db.password", "new-secret", true);
        when(configItemMapper.findByKeyAndEnvironment("db.password", "dev")).thenReturn(oldItem);
        when(encryptionService.encrypt("new-secret", "dev")).thenReturn("ENCRYPTED_NEW");

        configService.updateConfig(newItem);

        // 验证审计日志中旧值和新值都标记为加密
        verify(auditMapper).insert(argThat(log ->
            "***ENCRYPTED***".equals(log.getOldValue()) &&
            "***ENCRYPTED***".equals(log.getNewValue())
        ));
    }

    @Test
    void deleteConfig_retrievesOldValueAndRecordsAudit() {
        ConfigItem existing = createConfigItem("app.name", "value", false);
        when(configItemMapper.findByKeyAndEnvironment("app.name", "dev")).thenReturn(existing);

        configService.deleteConfig("app.name", "dev");

        verify(configItemMapper).delete("app.name", "dev");
        verify(cacheService).removeFromRedis("app.name", "dev");
        verify(auditMapper).insert(argThat(log ->
            "DELETE".equals(log.getChangeType()) &&
            "value".equals(log.getOldValue())
        ));
    }

    @Test
    void deleteConfig_whenConfigNotFound_noAuditOldValue() {
        when(configItemMapper.findByKeyAndEnvironment("ghost", "dev")).thenReturn(null);

        configService.deleteConfig("ghost", "dev");

        verify(configItemMapper).delete("ghost", "dev");
        verify(auditMapper).insert(argThat(log ->
            "DELETE".equals(log.getChangeType()) &&
            log.getOldValue() == null
        ));
    }

    @Test
    void getConfigsByEnvironment_returnsList() {
        List<ConfigItem> expected = Arrays.asList(
                createConfigItem("key1", "val1", false),
                createConfigItem("key2", "val2", true)
        );
        when(configItemMapper.findByEnvironment("dev")).thenReturn(expected);

        List<ConfigItem> result = configService.getConfigsByEnvironment("dev");

        assertEquals(2, result.size());
        verify(configItemMapper).findByEnvironment("dev");
    }

    @Test
    void isRedisAvailable_delegatesToCacheService() {
        when(cacheService.isRedisAvailable()).thenReturn(true);
        assertTrue(configService.isRedisAvailable());

        when(cacheService.isRedisAvailable()).thenReturn(false);
        assertFalse(configService.isRedisAvailable());
    }

    private ConfigItem createConfigItem(String key, String value, boolean encrypted) {
        ConfigItem item = new ConfigItem();
        item.setConfigKey(key);
        item.setConfigValue(value);
        item.setEnvironment("dev");
        item.setEncrypted(encrypted);
        item.setDescription("test");
        item.setVersion("1.0");
        item.setStatus("ACTIVE");
        return item;
    }
}
