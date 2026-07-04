/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2026 mbpz
 */

package com.crgmhrc.configcenter.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 灰度发布策略
 *
 * 允许按标签 (如 user-id, region) 或百分比将配置值分流到不同版本
 */
@Data
public class ConfigGrayRelease {
    private Long id;
    private String configKey;
    private String environment;

    // 灰度策略类型: PERCENTAGE (百分比) / TAG (标签匹配)
    private String strategyType;

    // 灰度策略详情 (JSON)
    // PERCENTAGE: {"percentage": 20}  // 20% 流量使用 grayValue
    // TAG: {"tag": "region", "value": "cn-north"}  // 匹配标签的使用 grayValue
    private String strategyDetail;

    // 灰度值 (分流后的配置值)
    private String grayValue;

    // 是否启用
    private Boolean enabled;

    private String operator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
