/**
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2026 mbpz
 */
package com.crgmhrc.configcenter.entity;

import java.time.LocalDateTime;

/**
 * 缓存信息实体类
 */
public class CacheInfo {
    private String key;
    private String environment;
    private String configKey;
    private String configValue;
    private String description;
    private String version;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long ttl; // 剩余过期时间（秒）
    private Long size; // 缓存大小（字节）

    public CacheInfo() {}

    public CacheInfo(String key, String environment, String configKey, String configValue, 
                    String description, String version, String status, 
                    LocalDateTime createTime, LocalDateTime updateTime, Long ttl, Long size) {
        this.key = key;
        this.environment = environment;
        this.configKey = configKey;
        this.configValue = configValue;
        this.description = description;
        this.version = version;
        this.status = status;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.ttl = ttl;
        this.size = size;
    }

    // Getters and Setters
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public Long getTtl() {
        return ttl;
    }

    public void setTtl(Long ttl) {
        this.ttl = ttl;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "CacheInfo{" +
                "key='" + key + '\'' +
                ", environment='" + environment + '\'' +
                ", configKey='" + configKey + '\'' +
                ", configValue='" + configValue + '\'' +
                ", description='" + description + '\'' +
                ", version='" + version + '\'' +
                ", status='" + status + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", ttl=" + ttl +
                ", size=" + size +
                '}';
    }
} 