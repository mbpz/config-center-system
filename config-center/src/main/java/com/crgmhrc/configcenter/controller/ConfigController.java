/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2026 mbpz
 */

package com.crgmhrc.configcenter.controller;

import com.crgmhrc.configcenter.entity.ConfigItem;
import com.crgmhrc.configcenter.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/configs")
@PreAuthorize("hasRole('ADMIN')")
public class ConfigController {

    @Autowired
    private ConfigService configService;

    @GetMapping("/{key}")
    public ResponseEntity<ConfigItem> getConfig(
            @PathVariable String key,
            @RequestParam(defaultValue = "dev") String environment) {
        ConfigItem config = configService.getConfig(key, environment);
        return config != null ? ResponseEntity.ok(config) : ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<List<ConfigItem>> getConfigs(
            @RequestParam(defaultValue = "dev") String environment) {
        return ResponseEntity.ok(configService.getConfigsByEnvironment(environment));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createConfig(@RequestBody ConfigItem configItem) {
        configService.createConfig(configItem);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "配置创建成功");
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{key}")
    public ResponseEntity<Map<String, Object>> updateConfig(
            @PathVariable String key,
            @RequestBody ConfigItem configItem) {
        configItem.setConfigKey(key);
        configService.updateConfig(configItem);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "配置更新成功");
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<Map<String, Object>> deleteConfig(
            @PathVariable String key,
            @RequestParam(defaultValue = "dev") String environment) {
        configService.deleteConfig(key, environment);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "配置删除成功");
        return ResponseEntity.ok(result);
    }
}
