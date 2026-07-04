/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2026 mbpz
 */

package com.crgmhrc.configcenter.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 安全工具类 - 获取当前认证用户信息
 */
public class SecurityUtils {

    /**
     * 获取当前认证用户名
     * @return 用户名，未认证返回 "anonymous"
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof UserDetails) {
            return ((UserDetails) authentication.getPrincipal()).getUsername();
        }
        return "anonymous";
    }

    /**
     * 检查当前用户是否有指定角色
     */
    public static boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }
}
