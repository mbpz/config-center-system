/*n * SPDX-License-Identifier: Apache-2.0n * Copyright 2026 mbpzn */

package com.crgmhrc.configcenter.mapper;

import com.crgmhrc.configcenter.entity.ConfigItem;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ConfigItemMapper {
    
    @Select("SELECT * FROM config_item WHERE config_key = #{key} AND environment = #{environment}")
    ConfigItem findByKeyAndEnvironment(@Param("key") String key, @Param("environment") String environment);
    
    @Select("SELECT * FROM config_item WHERE environment = #{environment}")
    List<ConfigItem> findByEnvironment(@Param("environment") String environment);
    
    @Insert("INSERT INTO config_item (config_key, config_value, description, environment, version, status, create_time, update_time) " +
            "VALUES (#{configKey}, #{configValue}, #{description}, #{environment}, #{version}, #{status}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ConfigItem configItem);
    
    @Update("UPDATE config_item SET config_value = #{configValue}, description = #{description}, " +
            "version = #{version}, status = #{status}, update_time = NOW() " +
            "WHERE config_key = #{configKey} AND environment = #{environment}")
    int update(ConfigItem configItem);
    
    @Delete("DELETE FROM config_item WHERE config_key = #{key} AND environment = #{environment}")
    int delete(@Param("key") String key, @Param("environment") String environment);
} 