/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2026 mbpz
 */

package com.crgmhrc.configcenter.mapper;

import com.crgmhrc.configcenter.entity.ConfigChangeLog;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AuditMapper {

    /**
     * 插入审计日志
     */
    @Insert("INSERT INTO config_change_log (config_key, environment, old_value, new_value, change_type, operator, change_time) " +
            "VALUES (#{configKey}, #{environment}, #{oldValue}, #{newValue}, #{changeType}, #{operator}, NOW())")
    int insert(ConfigChangeLog log);

    /**
     * 按配置键和环境查询变更历史
     */
    @Select("SELECT * FROM config_change_log " +
            "WHERE config_key = #{key} AND environment = #{environment} " +
            "ORDER BY change_time DESC LIMIT 100")
    List<ConfigChangeLog> findByKeyAndEnvironment(@Param("key") String key,
                                                   @Param("environment") String environment);

    /**
     * 查询所有变更历史（按时间倒序）
     */
    @Select("SELECT * FROM config_change_log ORDER BY change_time DESC LIMIT 100")
    List<ConfigChangeLog> findAll();
}
