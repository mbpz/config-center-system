/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2026 mbpz
 */

package com.crgmhrc.configcenter.client;

import com.crgmhrc.configcenter.client.exception.ConfigCenterException;
import com.crgmhrc.configcenter.client.model.ApiResponse;
import com.crgmhrc.configcenter.client.model.ConfigItem;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import okhttp3.*;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Config Center Java SDK 客户端
 *
 * 使用示例:
 * <pre>
 *   ConfigClient client = ConfigClient.builder()
 *       .serverUrl("http://localhost:8080")
 *       .username("admin")
 *       .password("admin123")
 *       .environment("dev")
 *       .build();
 *
 *   ConfigItem item = client.getConfig("app.name");
 *   System.out.println(item.getConfigValue());
 * </pre>
 */
public class ConfigClient {

    private static final String API_BASE = "/api/v1";
    private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final String serverUrl;
    private final String username;
    private final String password;
    private final String environment;
    private final OkHttpClient httpClient;

    private ConfigClient(Builder builder) {
        this.serverUrl = builder.serverUrl.replaceAll("/$", "");
        this.username = builder.username;
        this.password = builder.password;
        this.environment = builder.environment;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(builder.connectTimeout, TimeUnit.SECONDS)
                .readTimeout(builder.readTimeout, TimeUnit.SECONDS)
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    // ======================== 配置管理 ========================

    /**
     * 获取单个配置
     */
    public ConfigItem getConfig(String key) {
        Request request = new Request.Builder()
                .url(serverUrl + API_BASE + "/configs/" + key + "?environment=" + environment)
                .header("Authorization", basicAuth())
                .get()
                .build();
        return execute(request, new TypeReference<ConfigItem>() {});
    }

    /**
     * 获取配置列表
     */
    public List<ConfigItem> listConfigs(RequestCallback callback) {
        return listConfigs(environment);
    }

    public List<ConfigItem> listConfigs(String env) {
        Request request = new Request.Builder()
                .url(serverUrl + API_BASE + "/configs?environment=" + env)
                .header("Authorization", basicAuth())
                .get()
                .build();
        return execute(request, new TypeReference<List<ConfigItem>>() {});
    }

    /**
     * 创建配置
     */
    public void createConfig(ConfigItem item) {
        if (item.getEnvironment() == null) item.setEnvironment(environment);
        if (item.getVersion() == null) item.setVersion("1.0");
        if (item.getStatus() == null) item.setStatus("ACTIVE");

        RequestBody body = RequestBody.create(MAPPER.writeValueAsString(item), JSON);
        Request request = new Request.Builder()
                .url(serverUrl + API_BASE + "/configs")
                .header("Authorization", basicAuth())
                .post(body)
                .build();
        execute(request, new TypeReference<ApiResponse>() {});
    }

    /**
     * 更新配置
     */
    public void updateConfig(String key, ConfigItem item) {
        if (item.getEnvironment() == null) item.setEnvironment(environment);
        if (item.getConfigKey() == null) item.setConfigKey(key);

        RequestBody body = RequestBody.create(MAPPER.writeValueAsString(item), JSON);
        Request request = new Request.Builder()
                .url(serverUrl + API_BASE + "/configs/" + key + "?environment=" + environment)
                .header("Authorization", basicAuth())
                .put(body)
                .build();
        execute(request, new TypeReference<ApiResponse>() {});
    }

    /**
     * 删除配置
     */
    public void deleteConfig(String key) {
        Request request = new Request.Builder()
                .url(serverUrl + API_BASE + "/configs/" + key + "?environment=" + environment)
                .header("Authorization", basicAuth())
                .delete()
                .build();
        execute(request, new TypeReference<ApiResponse>() {});
    }

    // ======================== 配置分组 ========================

    /**
     * 获取配置分组列表 (需要序列化为 Map)
     */
    @SuppressWarnings("unchecked")
    public List<Object> listGroups() {
        Request request = new Request.Builder()
                .url(serverUrl + API_BASE + "/groups")
                .header("Authorization", basicAuth())
                .get()
                .build();
        return execute(request, new TypeReference<List<Object>>() {});
    }

    // ======================== 审计日志 ========================

    /**
     * 查询审计日志
     */
    public Object getAuditLog(String key, String env) {
        StringBuilder url = new StringBuilder(serverUrl + API_BASE + "/audit?");
        if (key != null && !key.isEmpty()) url.append("key=").append(key).append("&");
        if (env != null && !env.isEmpty()) url.append("environment=").append(env);

        Request request = new Request.Builder()
                .url(url.toString())
                .header("Authorization", basicAuth())
                .get()
                .build();
        return execute(request, new TypeReference<Object>() {});
    }

    // ======================== 健康检查 ========================

    /**
     * 检查服务健康状态
     */
    public boolean isHealthy() {
        try {
            Request request = new Request.Builder()
                    .url(serverUrl + API_BASE + "/health")
                    .get()
                    .build();
            execute(request, new TypeReference<Object>() {});
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ======================== 内部方法 ========================

    private String basicAuth() {
        return "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    }

    private <T> T execute(Request request, TypeReference<T> typeRef) {
        try (Response response = httpClient.newCall(request).execute()) {
            String body = response.body() != null ? response.body().string() : "";

            if (!response.isSuccessful()) {
                throw new ConfigCenterException(
                        "API call failed: " + request.url() + " - " + body,
                        response.code());
            }

            if (body == null || body.isEmpty()) {
                return null;
            }
            return MAPPER.readValue(body, typeRef);
        } catch (ConfigCenterException e) {
            throw e;
        } catch (IOException e) {
            throw new ConfigCenterException("Network error: " + e.getMessage(), e);
        }
    }

    @FunctionalInterface
    public interface RequestCallback {
        void onRequest(Request.Builder builder);
    }

    // ======================== Builder ========================

    public static class Builder {
        private String serverUrl = "http://localhost:8080";
        private String username = "admin";
        private String password = "admin";
        private String environment = "dev";
        private long connectTimeout = 10;
        private long readTimeout = 30;

        public Builder serverUrl(String serverUrl) { this.serverUrl = serverUrl; return this; }
        public Builder username(String username) { this.username = username; return this; }
        public Builder password(String password) { this.password = password; return this; }
        public Builder environment(String environment) { this.environment = environment; return this; }
        public Builder connectTimeout(long seconds) { this.connectTimeout = seconds; return this; }
        public Builder readTimeout(long seconds) { this.readTimeout = seconds; return this; }

        public ConfigClient build() {
            if (serverUrl == null || username == null || password == null) {
                throw new IllegalArgumentException("serverUrl, username, password are required");
            }
            return new ConfigClient(this);
        }
    }
}
