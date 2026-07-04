/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2026 mbpz
 */

package com.crgmhrc.configcenter.controller;

import com.crgmhrc.configcenter.entity.ConfigChangeLog;
import com.crgmhrc.configcenter.mapper.AuditMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/audit")
public class AuditController {

    @Autowired
    private AuditMapper auditMapper;

    /**
     * 查询指定配置的变更历史
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Map<String, Object>> getAuditLog(
            @RequestParam(required = false) String key,
            @RequestParam(required = false) String environment) {
        Map<String, Object> result = new HashMap<>();
        List<ConfigChangeLog> logs;

        if (key != null && !key.isEmpty() && environment != null) {
            logs = auditMapper.findByKeyAndEnvironment(key, environment);
        } else {
            logs = auditMapper.findAll();
        }

        result.put("success", true);
        result.put("data", logs);
        result.put("total", logs.size());
        return ResponseEntity.ok(result);
    }
}
