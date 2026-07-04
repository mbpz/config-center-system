/**
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2026 mbpz
 */
package com.crgmhrc.configcenter.controller;

import com.crgmhrc.configcenter.entity.ConfigItem;
import com.crgmhrc.configcenter.service.ConfigImportExportService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 配置批量导入导出
 *
 * 导出: GET /api/v1/configs/export?env=dev&format=json
 * 导入: POST /api/v1/configs/import (multipart file)
 */
@RestController
@RequestMapping("/api/v1/configs")
public class ConfigImportExportController {

    private final ConfigImportExportService importExportService;

    public ConfigImportExportController(ConfigImportExportService importExportService) {
        this.importExportService = importExportService;
    }

    /**
     * 导出配置
     * @param environment 环境 (dev/test/prod)
     * @param format json 或 yaml
     */
    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Resource> exportConfigs(
            @RequestParam(defaultValue = "dev") String environment,
            @RequestParam(defaultValue = "json") String format) {

        String content = importExportService.export(environment, format);
        String filename = "config-center-" + environment + "." + format;

        MediaType mediaType = "yaml".equalsIgnoreCase(format)
                ? MediaType.parseMediaType("application/x-yaml")
                : MediaType.APPLICATION_JSON;

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(new ByteArrayResource(content.getBytes()));
    }

    /**
     * 导入配置
     * @param file JSON/YAML 文件
     * @param environment 目标环境
     * @param overwrite 是否覆盖已有配置
     */
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> importConfigs(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "dev") String environment,
            @RequestParam(defaultValue = "false") boolean overwrite) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(createResponse(false, "文件不能为空"));
        }

        try {
            String content = new String(file.getBytes());
            String filename = file.getOriginalFilename();
            String format = (filename != null && filename.endsWith(".yaml")) ? "yaml" : "json";

            List<ConfigItem> imported = importExportService.importConfigs(
                    content, format, environment, overwrite);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "成功导入 " + imported.size() + " 个配置项");
            result.put("count", imported.size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createResponse(false, "导入失败: " + e.getMessage()));
        }
    }

    private Map<String, Object> createResponse(boolean success, String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", message);
        return result;
    }
}
