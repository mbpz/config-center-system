/*n * SPDX-License-Identifier: Apache-2.0n * Copyright 2026 mbpzn */

package com.crgmhrc.configcenter;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.crgmhrc.configcenter.mapper")
public class ConfigCenterApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConfigCenterApplication.class, args);
    }
} 