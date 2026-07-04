/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2026 mbpz
 */

package com.crgmhrc.configcenter.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * SSE (Server-Sent Events) 配置变更推送器
 *
 * 前端通过 EventSource('/api/v1/configs/stream') 订阅
 */
@Component
public class ConfigSseEmitter {

    private static final Logger logger = LoggerFactory.getLogger(ConfigSseEmitter.class);
    private static final long TIMEOUT_MS = 30 * 60 * 1000L; // 30 分钟超时

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 创建新的 SSE 连接
     */
    public SseEmitter createEmitter(String environment) {
        SseEmitter emitter = new SseEmitter(TIMEOUT_MS);

        // 注册关闭/超时回调
        emitter.onCompletion(() -> removeEmitter(emitter));
        emitter.onTimeout(() -> removeEmitter(emitter));
        emitter.onError(e -> removeEmitter(emitter));

        emitters.add(emitter);

        // 发送初始连接成功事件
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("{\"status\":\"connected\",\"environment\":\"" + environment + "\"}",
                          MediaType.APPLICATION_JSON));
        } catch (IOException e) {
            logger.warn("SSE 初始事件发送失败: {}", e.getMessage());
            removeEmitter(emitter);
        }

        logger.debug("SSE 连接建立: environment={}, total={}", environment, emitters.size());
        return emitter;
    }

    /**
     * 广播配置变更到所有订阅者
     */
    public void broadcast(ConfigChangeEvent event) {
        List<SseEmitter> dead = new CopyOnWriteArrayList<>();

        for (SseEmitter emitter : emitters) {
            try {
                String json = objectMapper.writeValueAsString(event);
                emitter.send(SseEmitter.event()
                        .name("config-change")
                        .id(String.valueOf(event.getTimestamp()))
                        .data(json, MediaType.APPLICATION_JSON));
            } catch (Exception e) {
                logger.debug("SSE 发送失败，移除连接: {}", e.getMessage());
                dead.add(emitter);
            }
        }

        emitters.removeAll(dead);
        if (!dead.isEmpty()) {
            logger.debug("SSE 清理 {} 个断开连接, 剩余 {}", dead.size(), emitters.size());
        }
    }

    private void removeEmitter(SseEmitter emitter) {
        emitters.remove(emitter);
    }

    /**
     * 获取当前活跃连接数
     */
    public int getActiveConnections() {
        return emitters.size();
    }
}
