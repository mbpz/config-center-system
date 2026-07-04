/**
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2026 mbpz
 */
package com.crgmhrc.configcenter.config;

/**
 * 配置变更事件 - 用于 SSE 推送
 */
public class ConfigChangeEvent {

    public enum Type { CREATED, UPDATED, DELETED }

    private final Type type;
    private final String configKey;
    private final String environment;
    private final String operator;
    private final long timestamp;

    public ConfigChangeEvent(Type type, String configKey, String environment, String operator) {
        this.type = type;
        this.configKey = configKey;
        this.environment = environment;
        this.operator = operator;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters
    public Type getType() { return type; }
    public String getConfigKey() { return configKey; }
    public String getEnvironment() { return environment; }
    public String getOperator() { return operator; }
    public long getTimestamp() { return timestamp; }
}
