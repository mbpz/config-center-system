/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2026 mbpz
 */

package com.crgmhrc.configcenter.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ConfigGroup {
    private Long id;
    private String groupId;
    private String groupName;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
