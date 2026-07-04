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
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
} 