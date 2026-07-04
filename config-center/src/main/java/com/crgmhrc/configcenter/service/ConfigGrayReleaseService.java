/**
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2026 mbpz
 */
package com.crgmhrc.configcenter.service;

import com.crgmhrc.configcenter.entity.ConfigGrayRelease;
import com.crgmhrc.configcenter.mapper.ConfigGrayReleaseMapper;
import com.crgmhrc.configcenter.security.SecurityUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ConfigGrayReleaseService {

    private static final Logger logger = LoggerFactory.getLogger(ConfigGrayReleaseService.class);
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    @Autowired
    private ConfigGrayReleaseMapper grayReleaseMapper;

    /**
     * 获取配置的实际值 (考虑灰度策略)
     *
     * @param configKey 配置键
     * @param environment 环境
     * @param originalValue 原始值
     * @param grayContext 灰度上下文 (包含 percent 或 tag 信息, 可为 null)
     * @return 实际值 (originalValue 或 grayValue)
     */
    public String resolveValue(String configKey, String environment,
                                String originalValue, GrayContext grayContext) {
        List<ConfigGrayRelease> releases = grayReleaseMapper.findActiveByKeyAndEnvironment(configKey, environment);
        if (releases.isEmpty()) {
            return originalValue;
        }

        for (ConfigGrayRelease release : releases) {
            if (!Boolean.TRUE.equals(release.getEnabled())) continue;

            try {
                if (matchesStrategy(release, grayContext)) {
                    logger.debug("灰度命中: key={}, strategy={}, operator={}",
                            configKey, release.getStrategyType(), release.getOperator());
                    return release.getGrayValue();
                }
            } catch (Exception e) {
                logger.warn("灰度策略匹配失败: key={}, error={}", configKey, e.getMessage());
            }
        }

        return originalValue;
    }

    /**
     * 判断灰度策略是否命中
     */
    private boolean matchesStrategy(ConfigGrayRelease release, GrayContext ctx) throws Exception {
        JsonNode detail = JSON_MAPPER.readTree(release.getStrategyDetail());

        if ("PERCENTAGE".equals(release.getStrategyType())) {
            int percentage = detail.path("percentage").asInt(0);
            if (percentage >= 100) return true;
            if (percentage <= 0) return false;

            // 使用一致性哈希 (基于用户标识或随机)
            int hash = ctx != null && ctx.getUserId() != null
                    ? Math.abs(ctx.getUserId().hashCode() % 100)
                    : ThreadLocalRandom.current().nextInt(100);
            return hash < percentage;

        } else if ("TAG".equals(release.getStrategyType())) {
            if (ctx == null || ctx.getTags() == null) return false;
            String tagKey = detail.path("tag").asText();
            String expectedValue = detail.path("value").asText();
            String actualValue = ctx.getTags().get(tagKey);
            return expectedValue.equals(actualValue);
        }

        return false;
    }

    public List<ConfigGrayRelease> getAllActive() {
        return grayReleaseMapper.findAllActive();
    }

    public List<ConfigGrayRelease> getAll() {
        return grayReleaseMapper.findAll();
    }

    public void createGrayRelease(ConfigGrayRelease release) {
        release.setOperator(SecurityUtils.getCurrentUsername());
        if (release.getEnabled() == null) release.setEnabled(true);
        logger.info("创建灰度策略: key={}, type={}, operator={}",
                release.getConfigKey(), release.getStrategyType(), release.getOperator());
        grayReleaseMapper.insert(release);
    }

    public void updateGrayRelease(ConfigGrayRelease release) {
        release.setOperator(SecurityUtils.getCurrentUsername());
        grayReleaseMapper.update(release);
    }

    public void deleteGrayRelease(Long id) {
        grayReleaseMapper.delete(id);
    }

    public void setEnabled(Long id, boolean enabled) {
        grayReleaseMapper.setEnabled(id, enabled);
    }

    /**
     * 灰度上下文 - 请求时携带的用户信息
     */
    public static class GrayContext {
        private String userId;
        private java.util.Map<String, String> tags;

        public GrayContext() {
            this.tags = new java.util.HashMap<>();
        }

        public GrayContext(String userId) {
            this();
            this.userId = userId;
        }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public java.util.Map<String, String> getTags() { return tags; }
        public void setTags(java.util.Map<String, String> tags) { this.tags = tags; }
        public void addTag(String key, String value) { this.tags.put(key, value); }
    }
}
