/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2026 mbpz
 */

package com.crgmhrc.configcenter.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * API 响应模型
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiResponse {
    private Boolean success;
    private String message;

    @JsonProperty("data")
    private Object data;

    private Integer total;
    private List<ConfigItem> configs;
    private Map<String, Object> extras;

    // Getters and Setters
    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }
    public Integer getTotal() { return total; }
    public void setTotal(Integer total) { this.total = total; }
    public List<ConfigItem> getConfigs() { return configs; }
    public void setConfigs(List<ConfigItem> configs) { this.configs = configs; }
    public Map<String, Object> getExtras() { return extras; }
    public void setExtras(Map<String, Object> extras) { this.extras = extras; }

    public boolean isSuccess() {
        return Boolean.TRUE.equals(success) || (success == null && data != null);
    }
}
