/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2026 mbpz
 */

package com.crgmhrc.configcenter.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Component
public class BootstrapTokenLogger implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(BootstrapTokenLogger.class);

    @Value("${CC_ADMIN_PASSWORD:}")
    private String configuredAdminPassword;

    @Override
    public void run(ApplicationArguments args) {
        if (configuredAdminPassword == null || configuredAdminPassword.isEmpty()
                || "admin".equals(configuredAdminPassword)) {
            // 未配置自定义密码，生成随机 bootstrap token
            String bootstrapToken = generateSecureToken();
            logger.info("");
            logger.info("╔══════════════════════════════════════════════════════════════╗");
            logger.info("║  Config Center - Bootstrap Admin Token                      ║");
            logger.info("║                                                              ║");
            logger.info("║  Username: admin                                             ║");
            logger.info("║  Password: {}                           ║", bootstrapToken);
            logger.info("║                                                              ║");
            logger.info("║  ⚠️  SAVE THIS TOKEN - it will not be shown again           ║");
            logger.info("║  Set CC_ADMIN_PASSWORD env var to disable bootstrap mode     ║");
            logger.info("╚══════════════════════════════════════════════════════════════╝");
            logger.info("");
        } else {
            logger.info("Config Center started with configured admin password.");
        }
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[24];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
