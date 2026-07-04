/**
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2026 mbpz
 */
package com.crgmhrc.configcenter.service;

import com.crgmhrc.configcenter.entity.ConfigGroup;
import com.crgmhrc.configcenter.mapper.ConfigGroupMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConfigGroupService {

    private static final Logger logger = LoggerFactory.getLogger(ConfigGroupService.class);

    @Autowired
    private ConfigGroupMapper configGroupMapper;

    public List<ConfigGroup> getAllGroups() {
        return configGroupMapper.findAll();
    }

    public ConfigGroup getGroupByGroupId(String groupId) {
        return configGroupMapper.findByGroupId(groupId);
    }

    public void createGroup(ConfigGroup group) {
        logger.info("创建配置组: groupId={}, name={}", group.getGroupId(), group.getGroupName());
        configGroupMapper.insert(group);
    }

    public void updateGroup(ConfigGroup group) {
        logger.info("更新配置组: groupId={}", group.getGroupId());
        configGroupMapper.update(group);
    }

    public void deleteGroup(String groupId) {
        logger.info("删除配置组: groupId={}", groupId);
        configGroupMapper.delete(groupId);
    }
}
