/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2026 mbpz
 */

package com.crgmhrc.configcenter.security;

/**
 * 租户上下文 - 线程级租户标识
 *
 * 当前版本: 固定返回 "default" (single-tenant 模式)
 * Phase 2 完整版: 从 HTTP Header (X-Tenant-Id) 或 JWT claim 提取
 */
public class TenantContext {

    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    public static final String DEFAULT_TENANT = "default";

    /**
     * 获取当前租户 ID
     */
    public static String getCurrentTenant() {
        String tenant = CURRENT_TENANT.get();
        return tenant != null ? tenant : DEFAULT_TENANT;
    }

    /**
     * 设置当前租户 ID
     */
    public static void setCurrentTenant(String tenantId) {
        CURRENT_TENANT.set(tenantId != null ? tenantId : DEFAULT_TENANT);
    }

    /**
     * 清除当前租户 (请求结束时调用)
     */
    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
