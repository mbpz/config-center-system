/**
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2026 mbpz
 */
package com.crgmhrc.configcenter.mapper;

import com.crgmhrc.configcenter.entity.ConfigGrayRelease;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ConfigGrayReleaseMapper {

    @Select("SELECT * FROM config_gray_release WHERE config_key = #{key} AND environment = #{env}")
    List<ConfigGrayRelease> findActiveByKeyAndEnvironment(@Param("key") String key, @Param("env") String environment);

    @Select("SELECT * FROM config_gray_release WHERE enabled = TRUE ORDER BY create_time DESC")
    List<ConfigGrayRelease> findAllActive();

    @Select("SELECT * FROM config_gray_release ORDER BY create_time DESC")
    List<ConfigGrayRelease> findAll();

    @Insert("INSERT INTO config_gray_release (config_key, environment, strategy_type, strategy_detail, gray_value, enabled, operator, create_time, update_time) " +
            "VALUES (#{configKey}, #{environment}, #{strategyType}, #{strategyDetail}, #{grayValue}, #{enabled}, #{operator}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ConfigGrayRelease release);

    @Update("UPDATE config_gray_release SET strategy_type = #{strategyType}, strategy_detail = #{strategyDetail}, " +
            "gray_value = #{grayValue}, enabled = #{enabled}, operator = #{operator}, update_time = NOW() " +
            "WHERE id = #{id}")
    int update(ConfigGrayRelease release);

    @Delete("DELETE FROM config_gray_release WHERE id = #{id}")
    int delete(@Param("id") Long id);

    @Update("UPDATE config_gray_release SET enabled = #{enabled}, update_time = NOW() WHERE id = #{id}")
    int setEnabled(@Param("id") Long id, @Param("enabled") Boolean enabled);
}
