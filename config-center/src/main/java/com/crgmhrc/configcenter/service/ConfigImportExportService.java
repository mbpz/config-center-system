/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2026 mbpz
 */

package com.crgmhrc.configcenter.service;

import com.crgmhrc.configcenter.entity.ConfigItem;
import com.crgmhrc.configcenter.mapper.ConfigItemMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConfigImportExportService {

    private static final Logger logger = LoggerFactory.getLogger(ConfigImportExportService.class);
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);
    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory())
            .enable(SerializationFeature.INDENT_OUTPUT)
            .findAndRegisterModules();

    @Autowired
    private ConfigItemMapper configItemMapper;

    @Autowired
    private ConfigService configService;

    /**
     * 导出指定环境的配置为 JSON 或 YAML
     */
    public String export(String environment, String format) {
        List<ConfigItem> configs = configItemMapper.findByEnvironment(environment);

        try {
            Map<String, Object> export = new LinkedHashMap<>();
            export.put("environment", environment);
            export.put("version", "1.0");
            export.put("configs", configsToMap(configs));

            if ("yaml".equalsIgnoreCase(format)) {
                return YAML_MAPPER.writeValueAsString(export);
            } else {
                return JSON_MAPPER.writeValueAsString(export);
            }
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("导出失败: " + e.getMessage(), e);
        }
    }

    /**
     * 从 JSON/YAML 内容导入配置
     */
    @Transactional
    public List<ConfigItem> importConfigs(String content, String format,
                                           String environment, boolean overwrite) {
        try {
            Map<String, Object> data;
            if ("yaml".equalsIgnoreCase(format)) {
                data = YAML_MAPPER.readValue(content, new TypeReference<Map<String, Object>>() {});
            } else {
                data = JSON_MAPPER.readValue(content, new TypeReference<Map<String, Object>>() {});
            }

            List<Map<String, Object>> configList = extractConfigs(data);
            List<ConfigItem> imported = new ArrayList<>();

            for (Map<String, Object> itemMap : configList) {
                ConfigItem item = new ConfigItem();
                item.setConfigKey((String) itemMap.getOrDefault("configKey",
                        itemMap.getOrDefault("key", "")));
                item.setConfigValue(itemMap.getOrDefault("configValue",
                        itemMap.getOrDefault("value", "")).toString());
                item.setDescription((String) itemMap.getOrDefault("description", ""));
                item.setEnvironment(environment);
                item.setVersion((String) itemMap.getOrDefault("version", "1.0"));
                item.setStatus((String) itemMap.getOrDefault("status", "ACTIVE"));
                item.setEncrypted(Boolean.TRUE.equals(itemMap.get("encrypted")));

                if (item.getConfigKey() == null || item.getConfigKey().isEmpty()) {
                    continue;
                }

                // 检查是否已存在
                ConfigItem existing = configItemMapper.findByKeyAndEnvironment(
                        item.getConfigKey(), environment);
                if (existing != null) {
                    if (overwrite) {
                        configService.updateConfig(item);
                        imported.add(item);
                    }
                } else {
                    configService.createConfig(item);
                    imported.add(item);
                }
            }

            logger.info("导入完成: env={}, count={}, overwrite={}", environment, imported.size(), overwrite);
            return imported;
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("解析失败 (格式: " + format + "): " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractConfigs(Map<String, Object> data) {
        Object configs = data.get("configs");
        if (configs instanceof List) {
            return (List<Map<String, Object>>) configs;
        }
        // 兼容简单数组格式
        if (data instanceof List) {
            return (List<Map<String, Object>>) data;
        }
        return new ArrayList<>();
    }

    private List<Map<String, Object>> configsToMap(List<ConfigItem> configs) {
        return configs.stream().map(item -> {
            Map<String, Object> map = new java.util.LinkedHashMap<>();
            map.put("configKey", item.getConfigKey());
            map.put("configValue", item.getConfigValue());
            map.put("description", item.getDescription());
            map.put("version", item.getVersion());
            map.put("status", item.getStatus());
            map.put("encrypted", item.getEncrypted());
            return map;
        }).collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }
}
