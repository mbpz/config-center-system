/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2026 mbpz
 */

package com.crgmhrc.configcenter.controller;

import com.crgmhrc.configcenter.entity.ConfigGrayRelease;
import com.crgmhrc.configcenter.service.ConfigGrayReleaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/gray-releases")
public class ConfigGrayReleaseController {

    private final ConfigGrayReleaseService grayReleaseService;

    public ConfigGrayReleaseController(ConfigGrayReleaseService grayReleaseService) {
        this.grayReleaseService = grayReleaseService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<List<ConfigGrayRelease>> getAllActive() {
        return ResponseEntity.ok(grayReleaseService.getAllActive());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> create(@RequestBody ConfigGrayRelease release) {
        grayReleaseService.createGrayRelease(release);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "灰度策略创建成功");
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id,
                                                       @RequestBody ConfigGrayRelease release) {
        release.setId(id);
        grayReleaseService.updateGrayRelease(release);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "灰度策略更新成功");
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        grayReleaseService.deleteGrayRelease(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "灰度策略删除成功");
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> toggle(@PathVariable Long id,
                                                      @RequestParam boolean enabled) {
        grayReleaseService.setEnabled(id, enabled);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", enabled ? "已启用" : "已禁用");
        return ResponseEntity.ok(result);
    }
}
