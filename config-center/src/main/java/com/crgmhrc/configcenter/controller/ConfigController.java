/*n * SPDX-License-Identifier: Apache-2.0n * Copyright 2026 mbpzn */

package com.crgmhrc.configcenter.controller;

import com.crgmhrc.configcenter.entity.ConfigItem;
import com.crgmhrc.configcenter.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/configs")
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
    public ResponseEntity<Void> createConfig(@RequestBody ConfigItem configItem) {
        configService.createConfig(configItem);
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/{key}")
    public ResponseEntity<Void> updateConfig(
            @PathVariable String key,
            @RequestBody ConfigItem configItem) {
        configItem.setConfigKey(key);
        configService.updateConfig(configItem);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/{key}")
    public ResponseEntity<Void> deleteConfig(
            @PathVariable String key,
            @RequestParam(defaultValue = "dev") String environment) {
        configService.deleteConfig(key, environment);
        return ResponseEntity.ok().build();
    }
} 