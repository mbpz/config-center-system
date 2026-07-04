/**
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2026 mbpz
 */
package com.crgmhrc.configcenter.controller;

import com.crgmhrc.configcenter.config.ConfigSseEmitter;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Server-Sent Events 配置流
 *
 * 前端使用:
 *   const es = new EventSource('/api/v1/configs/stream');
 *   es.addEventListener('config-change', (e) => {
 *     const event = JSON.parse(e.data);
 *     console.log('Config changed:', event.type, event.configKey);
 *   });
 */
@RestController
@RequestMapping("/api/v1/configs/stream")
public class ConfigStreamController {

    private final ConfigSseEmitter sseEmitter;

    public ConfigStreamController(ConfigSseEmitter sseEmitter) {
        this.sseEmitter = sseEmitter;
    }

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public SseEmitter stream(@RequestParam(defaultValue = "dev") String environment) {
        return sseEmitter.createEmitter(environment);
    }
}
