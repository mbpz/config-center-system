/**
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2026 mbpz
 */
package com.crgmhrc.configcenter.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 配置变更事件监听器 - 转发到 SSE 推送器
 */
@Component
public class ConfigChangeEventListener {

    private static final Logger logger = LoggerFactory.getLogger(ConfigChangeEventListener.class);

    private final ConfigSseEmitter sseEmitter;

    public ConfigChangeEventListener(ConfigSseEmitter sseEmitter) {
        this.sseEmitter = sseEmitter;
    }

    @Async
    @EventListener
    public void onConfigChange(ConfigChangeEvent event) {
        logger.debug("配置变更事件: type={}, key={}, env={}, operator={}",
                event.getType(), event.getConfigKey(), event.getEnvironment(), event.getOperator());
        sseEmitter.broadcast(event);
    }
}
