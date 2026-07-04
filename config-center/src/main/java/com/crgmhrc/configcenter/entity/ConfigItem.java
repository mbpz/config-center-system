/*n * SPDX-License-Identifier: Apache-2.0n * Copyright 2026 mbpzn */

package com.crgmhrc.configcenter.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ConfigItem {
    private Long id;
    private String configKey;
    private String configValue;
    private String description;
    private String environment;
    private String version;
    private String status;
    /**
     * 是否加密存储
     * true: config_value 字段在数据库中为 AES-256-GCM 密文
     */
    private Boolean encrypted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
} 