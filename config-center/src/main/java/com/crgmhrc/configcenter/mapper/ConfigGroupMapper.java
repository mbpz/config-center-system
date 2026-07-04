/**
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2026 mbpz
 */
package com.crgmhrc.configcenter.mapper;

import com.crgmhrc.configcenter.entity.ConfigGroup;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ConfigGroupMapper {

    @Select("SELECT * FROM config_group WHERE group_id = #{groupId}")
    ConfigGroup findByGroupId(@Param("groupId") String groupId);

    @Select("SELECT * FROM config_group ORDER BY create_time DESC")
    List<ConfigGroup> findAll();

    @Insert("INSERT INTO config_group (group_id, group_name, description, create_time, update_time) " +
            "VALUES (#{groupId}, #{groupName}, #{description}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ConfigGroup group);

    @Update("UPDATE config_group SET group_name = #{groupName}, description = #{description}, " +
            "update_time = NOW() WHERE group_id = #{groupId}")
    int update(ConfigGroup group);

    @Delete("DELETE FROM config_group WHERE group_id = #{groupId}")
    int delete(@Param("groupId") String groupId);
}
