/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2026 mbpz
 */

package com.crgmhrc.configcenter.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ConfigChangeLog {
    private Long id;
    private String configKey;
    private String environment;
    private String oldValue;
    private String newValue;
    private String changeType; // CREATE / UPDATE / DELETE
    private String operator;
    private LocalDateTime changeTime;
}
