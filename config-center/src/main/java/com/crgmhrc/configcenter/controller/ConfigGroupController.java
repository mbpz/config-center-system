/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2026 mbpz
 */

package com.crgmhrc.configcenter.controller;

import com.crgmhrc.configcenter.entity.ConfigGroup;
import com.crgmhrc.configcenter.service.ConfigGroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/groups")
public class ConfigGroupController {

    private final ConfigGroupService groupService;

    public ConfigGroupController(ConfigGroupService groupService) {
        this.groupService = groupService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<List<ConfigGroup>> getAllGroups() {
        return ResponseEntity.ok(groupService.getAllGroups());
    }

    @GetMapping("/{groupId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<ConfigGroup> getGroup(@PathVariable String groupId) {
        ConfigGroup group = groupService.getGroupByGroupId(groupId);
        return group != null ? ResponseEntity.ok(group) : ResponseEntity.notFound().build();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createGroup(@RequestBody ConfigGroup group) {
        groupService.createGroup(group);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "配置组创建成功");
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{groupId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateGroup(
            @PathVariable String groupId, @RequestBody ConfigGroup group) {
        group.setGroupId(groupId);
        groupService.updateGroup(group);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "配置组更新成功");
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{groupId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteGroup(@PathVariable String groupId) {
        groupService.deleteGroup(groupId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "配置组删除成功");
        return ResponseEntity.ok(result);
    }
}
